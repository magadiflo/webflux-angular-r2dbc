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

