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