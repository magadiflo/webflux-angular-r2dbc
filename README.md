# [Programación reactiva con Spring Data R2DBC](https://medium.com/pictet-technologies-blog/reactive-programming-with-spring-data-r2dbc-ee9f1c24848b)

- Tutorial tomado de la página `Medium` del autor `Alexandre Jacquot`.
- Repositorio del tutorial
  [reactive-todo-list-r2dbc](https://github.com/pictet-technologies-open-source/reactive-todo-list-r2dbc)

---

Cómo construir una aplicación web colaborativa con `Angular`, `Spring Boot`, `Spring WebFlux`, `Spring Data R2DBC` y
`PostgreSQL`.

En un tutorial anterior del mismo autor trabajamos en una aplicación, misma que está en el siguiente repositorio
[webflux-angular-mongodb](https://github.com/magadiflo/webflux-angular-mongodb), donde solo usamos un tipo de
objeto de dominio. Por lo tanto, todas las operaciones relacionadas con la capa de acceso a datos procesaban un solo
documento a la vez. Este era un caso de uso ideal para la programación reactiva, pero **¿qué pasaría si introdujéramos
relaciones entre objetos?** Construir un objeto y tener que recuperar sus objetos relacionados antes de poder enviarlo
de vuelta no parece cumplir con la especificación de `Reactive Streams`. Además, **¿cómo podemos manejar correctamente
las transacciones cuando los datos se guardan mediante varios subprocesos? ¿Seguirá siendo posible la programación
reactiva o será necesario volver a la programación imperativa?**

En este artículo, intentaremos responder a todas estas preguntas. Actualizaremos nuestra aplicación web colaborativa y
utilizaremos una base de datos relacional. Lo construiremos con `Angular`, `Spring Boot`, `Spring WebFlux`,
`Spring Data R2DBC` y `PostgreSQL`. Para hacer frente a las modificaciones concurrentes utilizaremos el
`bloqueo optimista`, los `eventos` enviados por el servidor y el `sistema de notificaciones PostgreSQL`.

---

## R2DBC (Reactive Relational Database Connectivity)

La documentación oficial dice lo siguiente:

- `Basado en la especificación Reactive Streams`. `R2DBC` se basa en la especificación Reactive Streams, que proporciona
  una API no bloqueante y totalmente reactiva.


- `Funciona con bases de datos relacionales`. A diferencia de la naturaleza bloqueante de `JDBC`, `R2DBC` le permite
  trabajar con bases de datos SQL mediante una API reactiva.


- `Admite soluciones escalables`. Con `Reactive Streams`, `R2DBC` le permite pasar del modelo clásico de
  `un hilo por conexión` a un enfoque más potente y escalable.


- `Proporciona una especificación abierta`. `R2DBC` es una especificación abierta y establece una interfaz de proveedor
  de servicios (SPI) para que los proveedores de controladores implementen y los clientes consuman.

## Crea contenedor de PostgreSQL con Docker Compose

Para almacenar nuestros datos, utilizaremos `PostgreSQL`. Nuestro primer paso consiste en configurar una instancia local
de `PostgreSQL` usando `Docker Compose`. Para ello, necesitamos crear el siguiente archivo `compose.yml`.

````yml
services:
  postgres:
    image: postgres:15.2-alpine
    container_name: c-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: db_webflux_angular_r2dbc
      POSTGRES_USER: magadiflo
      POSTGRES_PASSWORD: magadiflo
    ports:
      - '5433:5432'
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
    name: postgres_data
````

Como se observa en el archivo anterior, estamos creando un servicio de postgres que creará nuestro contenedor. En el
tutorial define otro servicio para crear una instancia de `PgAdmin`, en nuestro caso no lo requerimos, dado que
usaré `DBeaver` que ya tengo instalada en mi pc, para interactuar con la base de datos de postgres.

Iniciamos el contenedor ejecutamos el siguiente comando en la misma carpeta del archivo `compose.yml` y luego
verificamos que el contenedor se haya creado.

````bash
$ docker compose up -d                           
 
$ docker container ls -a
CONTAINER ID   IMAGE                  COMMAND                  CREATED          STATUS                           PORTS                    NAMES
5ad2963bd825   postgres:15.2-alpine   "docker-entrypoint.s…"   33 seconds ago   Up 31 seconds                    0.0.0.0:5433->5432/tcp   c-postgres
````

Ahora, usando `DBeaver` nos conectamos a la base de datos de `PostgresSQL` que está corriendo en su propio contenedor de
docker.

![01.png](assets/01.png)

## Crea una aplicación web reactiva con Spring Data R2DBC

Para demostrar cómo lidiar con las relaciones en la programación reactiva, necesitamos enriquecer nuestro modelo de
datos. En la versión anterior de nuestra aplicación, los usuarios podían trabajar juntos en una lista de tareas
compartida. Podrían realizar las siguientes acciones:

- Agregar un item a la lista compartida.
- Editar la descripción de un item.
- Cambiar el estado de un item.
- Eliminar un item.

En la nueva versión que vamos a desarrollar, también van a poder editar los ítems con el fin de:

- Definir una persona asignada (opcional)
- Definir un conjunto de etiquetas como `Private`, `Sport`, `Work`, etc. (0 a n elementos)

## Agrega proyecto de Spring Boot WebFlux

Utilizando la página de `spring initializr` creamos el proyecto de `Spring Boot` con las siguientes dependencias.

````xml

<project>
    <!--Spring Boot 3.3.3-->
    <!--java.version 21-->
    <!--org.mapstruct.version 1.6.0-->
    <!--lombok-mapstruct-binding.version 0.2.0-->
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-r2dbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        <dependency>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
        </dependency>

        <!--Agregado manualmente-->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${org.mapstruct.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
            <version>2.6.0</version>
        </dependency>
        <!--/Agregado manualmente-->

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>r2dbc-postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <!--MapStruct-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven-compiler-plugin.version}</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${org.mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>${lombok-mapstruct-binding.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <!--/MapStruct-->
        </plugins>
    </build>

</project>
````

Notar que he agregado manualmente la dependencia de `MapStruct` y de `Swagger (OpenAPI)`. Con respecto a la dependencia
de `MapStruct`, ha sido necesario la configuración del plugin de `maven-compiler-plugin`. Para mayor información
sobre el porqué de esta configuración visitar el repositorio
[webflux-angular-mongodb](https://github.com/magadiflo/webflux-angular-mongodb/blob/main/README.md).

## El modelo de datos

Vamos a crear las distintas entidades que utilizaremos en este proyecto.

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "items")
public class Item {
    @Id
    private Long id;
    private String description;
    @Builder.Default
    private ItemStatus status = ItemStatus.TO_DO;
    private Long assigneeId;

    @Transient
    private Person assignee;
    @Transient
    private List<Tag> tags;

    @Version
    private Long version;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
````

````java
public enum ItemStatus {
    TO_DO,
    IN_PROGRESS,
    DONE
}
````

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "persons")
public class Person {
    @Id
    private Long id;
    private String firstName;
    private String lastName;

    @Version
    private Long version;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
````

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tags")
public class Tag {
    @Id
    private Long id;
    private String name;

    @Version
    private Long version;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
````

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "item_tags")
public class ItemTag {
    @Id
    private Long id;
    private Long itemId;
    private Long tagId;
}
````

Lo primero que hay que notar es el uso de la anotación `@Table`. Aunque no es obligatorio anotar nuestros objetos de
dominio, no hacerlo afectaría el rendimiento. En realidad, esta anotación es utilizada por el marco de mapeo para
preprocesar los objetos de dominio con el fin de extraer los metadatos necesarios para interactuar con la base de datos.

Otra anotación importante es `@Id`. Se utiliza para mapear un campo de clase a la `clave primaria de la tabla`. Tenga
en cuenta que, con `Spring Data R2DBC`, no hay generación automática de identificadores únicos. Ni siquiera podemos
especificar una estrategia de generación. Para resolver este problema, simplemente podemos indicarle a `PostgreSQL` que
genere automáticamente el `ID` cuando se crea un registro utilizando el tipo de datos `BIGSERIAL` (porque usamos Long,
si usáramos Integer usaríamos el SERIAL).

Además, `Spring Data R2DBC no admite identificadores embebidos`. En otras palabras, no podemos definir una clave
principal compuesta. Esta limitación es bastante molesta porque nos obliga a utilizar un ID único generado en su lugar
y esto tiene un impacto directo en la cantidad de código que necesitamos escribir. Verá este impacto con más detalle
cuando guardemos algunos registros en la tabla `item_tags`. Esta tabla, que se utiliza para representar la relación
de `muchos a muchos` entre `items` y `tags`, tiene una clave técnica, mientras que podríamos haber utilizado el ID del
item y el ID del tag como clave principal compuesta.

Las entidades `Item`, `Person` y `Tag` tienen la anotación `@Version` sobre un campo de tipo `Long`. Esta anotación
es totalmente compatible y viene con un mecanismo de `bloqueo optimista`. Cada vez que se va a guardar un registro, se
compara la versión actual del registro con la proporcionada y, si son idénticas, se incrementa la versión y se guarda el
registro. Si son diferentes, el registro no se guarda y se devuelve un error.

Las anotaciones de auditoría como `@CreatedDate` o `@LastModifiedDate` también son compatibles. Para habilitar la
función de auditoría, debemos declararla explícitamente con la anotación `@EnableR2dbcAuditing`. Podemos, por ejemplo,
agregarla sobre la clase principal de la aplicación.

````java

@EnableR2dbcAuditing
@SpringBootApplication
public class TodoListBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoListBackendApplication.class, args);
    }
}
````

Un aspecto muy importante que hay que tener en cuenta antes de decidirse a utilizar `Spring Data R2DBC` es la falta de
compatibilidad con relaciones. A diferencia de `Spring Data JPA`, no es posible utilizar un marco `ORM` avanzado como
Hibernate. `Spring Data R2DBC` es un mapeador de objetos simple y limitado. Como consecuencia, muchas de las funciones
que suelen ofrecer los marcos ORM no están disponibles, como, por ejemplo, el almacenamiento en caché o la carga
diferida. Como los objetos relacionados no se pueden mapear automáticamente, los campos `assignee` y `tags` deben
anotarse con `@Transient` para indicarle al marco de mapeo que los ignore. En la siguiente sección, veremos cómo
mapear estos objetos.

## [Versionado de base de datos con Liquibase](https://github.com/magadiflo/spring-boot-liquibase/blob/main/README.md)

Por último, pero no por ello menos importante, `el esquema de la base de datos no se puede crear automáticamente en
función de los objetos del dominio`. Para superar este problema, podemos utilizar `Liquibase` para crear y mantener
nuestro esquema.

Recordemos que cuando creamos el proyecto, agregamos la dependencia de `Spring Data R2DBC`. Ahora, adicionalmente
agregamos la dependencia de `Liquibase` y en automático se nos agrega la dependencia `spring-jdbc`, es porque
`Liquibase`, por defecto, utiliza `JDBC` para interactuar con la base de datos. Aunque estemos utilizando
`Spring Data R2DBC` para operaciones reactivas, `Liquibase` aún requiere `JDBC` para ejecutar sus scripts de migración
de base de datos.

`Spring Data R2DBC` y `Spring WebFlux` están diseñados para operaciones reactivas y no bloqueantes, pero `Liquibase` no
tiene soporte nativo para `R2DBC` y depende de `JDBC` para sus operaciones. Por eso, al agregar `Liquibase` a tu
proyecto, también se agrega `spring-jdbc` como dependencia.

````xml

<dependencies>
    <dependency>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-jdbc</artifactId>
    </dependency>
</dependencies>
````

Otro punto importante es que cuando generamos el proyecto agregando la dependencia de `Liquibase` desde
`Spring Initializr`, se nos crea el directorio `src/main/resources/db/changelog`. Este directorio será el que usaremos
para crear nuestros archivos de migración.

Iniciemos creando un archivo maestro llamado `db.changelog-master.yml`, será el que hará referencia a todos los archivos
de `changeLog` en el orden adecuado. Estos archivos `changeLog` estarán ubicados en el directorio definido en el `path`.
El contenido y la ubicación de este archivo será el siguiente:

````yml
# src/main/resources/db/db.changelog-master.yml
databaseChangeLog:
  - includeAll:
      path: /db/changelog/
````

Dentro del directorio `/db/changelog/` crearemos nuestra primera migración. El archivo que crearemos tendrá una
nomenclatura propia, es decir `<migration_number>_<what_does_this_migration_do>.yml`.

El `changeSet` que crearemos con esta migración será crear la tabla `persons` en la base de datos. Para eso, tomando
como referencia nuestra entidad `Person`, agregamos las configuraciones necesarias para crear la tabla.

````yml
# src/main/resources/db/changelog/1_create_persons_table.yml
databaseChangeLog:
  - changeSet:
      id: 1_create_persons_table
      author: Martín
      changes:
        - createTable:
            tableName: persons
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: first_name
                  type: VARCHAR(100)
                  constraints:
                    nullable: false
              - column:
                  name: last_name
                  type: VARCHAR(100)
                  constraints:
                    nullable: false
              - column:
                  name: version
                  type: BIGINT
                  defaultValue: 0
                  constraints:
                    nullable: false
              - column:
                  name: created_date
                  type: TIMESTAMP
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: last_modified_date
                  type: TIMESTAMP
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
````

Como último paso para ejecutar la aplicación y ver la creación de la tabla usando `Liquibase`, es precisamente
configurar `Liquibase` en el `application.yml`, donde le definiremos el archivo maestro y las propiedades de conexión
`jdbc` para conectarse a la base de datos.

A continuación, se muestra las propiedades completas definidas en el `application.yml` de nuestra aplicación.

````yml
server:
  port: 8080
  error:
    include-message: always

spring:
  application:
    name: todo-list-backend

  # Configuración de controlador R2DBC
  r2dbc:
    url: r2dbc:postgresql://localhost:5433/db_webflux_angular_r2dbc
    username: magadiflo
    password: magadiflo

  # Liquibase (Actualización de esquema)
  liquibase:
    change-log: classpath:/db/db.changelog-master.yml
    url: jdbc:postgresql://localhost:5433/db_webflux_angular_r2dbc
    user: magadiflo
    password: magadiflo

# Logging
logging:
  level:
    dev.magadiflo.app: DEBUG
    io.r2dbc.postgresql.QUERY: DEBUG
    io.r2dbc.postgresql.PARAM: DEBUG
````
