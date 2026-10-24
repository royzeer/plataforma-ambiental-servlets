# Módulo de Proyectos con Servlets y JSP

[![Build](https://github.com/royzeer/plataforma-ambiental-servlets/actions/workflows/build.yml/badge.svg)](https://github.com/royzeer/plataforma-ambiental-servlets/actions/workflows/build.yml)
![Java 17](https://img.shields.io/badge/Java_17-ED8B00?logo=openjdk&logoColor=white)
![Jakarta Servlet 6](https://img.shields.io/badge/Jakarta_Servlet_6-007396?logo=eclipsejakartaee&logoColor=white)
![JSP + JSTL](https://img.shields.io/badge/JSP_%2B_JSTL-007396?logo=eclipsejakartaee&logoColor=white)
![JDBC](https://img.shields.io/badge/JDBC-4479A1?logo=mysql&logoColor=white)
![MySQL 8](https://img.shields.io/badge/MySQL_8-4479A1?logo=mysql&logoColor=white)
![Tomcat 10.1](https://img.shields.io/badge/Tomcat_10.1-F8DC75?logo=apachetomcat&logoColor=black)
![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white)
![licencia MIT](https://img.shields.io/badge/licencia_MIT-blue?logo=opensourceinitiative&logoColor=white)

> *English summary at the [end of this file](#english-summary).*

**Plataforma Digital Integral para la Gestión de Proyectos de Impacto Ambiental**

| | |
|---|---|
| **Programa** | Análisis y Desarrollo de Software (228118) – SENA |
| **Evidencia** | GA7-220501096-AA2-EV02 – Módulos de software codificados y probados |
| **Aprendices** | Roycer Cervantes · Sebastián Rodríguez |
| **Tecnologías** | Java 17 · Jakarta Servlet 6 · JSP + JSTL · JDBC · MySQL 8 · Apache Tomcat 10.1 · Maven |

Este módulo es la parte web (Java) que administra los **proyectos ambientales** de la plataforma: registrar, consultar, buscar, editar y eliminar. Usa la **misma base de datos** (`impacto_ambiental_db`) que la API REST del proyecto, así que lo que se crea aquí también lo ve el front en React.

## Qué pide la evidencia y dónde está

| Requisito de la guía | Dónde se cumple |
|---|---|
| Formularios HTML con servlets | `formulario.jsp`, `lista.jsp` (buscador) y `eliminar.jsp` envían a `ProyectoServlet` |
| Método **GET** | `doGet`: listar, buscar (`?buscar=&estado=`), ver detalle, abrir formularios |
| Método **POST** | `doPost`: crear, actualizar y eliminar (nunca se borra con un enlace GET) |
| Elementos de JSP | Directivas (`<%@ page %>`, `<%@ include %>`, `<%@ taglib %>`), acción `<jsp:include>`, declaración `<%! %>`, scriptlet `<% %>`, expresión `<%= %>`, Expression Language `${}` y JSTL (`c:forEach`, `c:if`, `c:choose`, `fmt:formatNumber`) |
| Conexión a BD | JDBC con `PreparedStatement` (clase `ConexionBD` + DAOs) |
| Estándares de codificación | Paquetes en minúscula, clases en PascalCase, métodos y variables en camelCase, constantes en MAYÚSCULA, comentarios Javadoc |
| Versionamiento | Repositorio Git con commits por cada parte del módulo |

## Estructura

```
src/main/java/co/edu/sena/impactoambiental/
├── config/ConexionBD.java        # abre conexiones JDBC (lee db.properties)
├── modelo/Proyecto.java          # JavaBean del proyecto
├── modelo/Usuario.java           # responsable del proyecto
├── dao/ProyectoDAO.java          # INSERT, SELECT, UPDATE, DELETE
├── dao/UsuarioDAO.java           # responsables válidos
├── util/ValidadorProyecto.java   # validaciones del lado del servidor
└── servlet/ProyectoServlet.java  # controlador: doGet / doPost
src/main/resources/db.properties  # datos de conexión a MySQL
src/main/webapp/
├── WEB-INF/web.xml               # páginas de error, encoding, sesión
├── WEB-INF/vistas/comunes/       # encabezado (include) y pie (jsp:include)
├── WEB-INF/vistas/proyectos/     # lista, formulario, detalle, eliminar
├── css/estilos.css
├── index.jsp                     # redirige a /proyectos
└── error.jsp
database/schema.sql               # tablas (las mismas de la API)
database/datos_prueba.sql         # usuarios y 3 proyectos de ejemplo
```

## Cómo ejecutarlo

1. **Base de datos.** En MySQL Workbench o phpMyAdmin ejecutar `database/schema.sql` y luego `database/datos_prueba.sql`.
   (Si ya tienen la API corriendo con `npm run seed`, este paso ya está hecho.)
2. **Conexión.** Abrir `src/main/resources/db.properties` y poner el usuario y la contraseña de su MySQL.
3. **Compilar.** `mvn clean package` → genera `target/plataforma-ambiental.war`.
4. **Desplegar.** Copiar el `.war` a la carpeta `webapps` de **Tomcat 10.1** y arrancar Tomcat
   (o en NetBeans / IntelliJ: *Run on Server* con Tomcat 10.1).
5. Abrir **http://localhost:8080/plataforma-ambiental/**

> Importante: se necesita Tomcat **10.1** (usa `jakarta.servlet`). En Tomcat 9 no funciona porque ese usa `javax.servlet`.

## Pruebas realizadas

| # | Prueba | Método | Resultado |
|---|---|---|---|
| 1 | Listar proyectos | GET `/proyectos` | Muestra los 3 proyectos de la base de datos |
| 2 | Buscar por texto ("gaira") | GET `/proyectos?buscar=gaira` | 1 resultado |
| 3 | Enviar formulario con datos malos (nombre con `<>`, municipio con números, fecha fin antes del inicio, presupuesto negativo, sin tipo ni responsable) | POST `accion=guardar` | Se devuelve el formulario con 7 mensajes de error, sin guardar nada |
| 4 | Registrar proyecto válido | POST `accion=guardar` | Se guarda, se genera el código `PIA-2026-0004` y abre el detalle |
| 5 | Editar presupuesto y estado | POST `accion=guardar` con `id` | "Los cambios del proyecto se guardaron." |
| 6 | Eliminar con confirmación | GET confirmación → POST `accion=eliminar` | "Se eliminó el proyecto ..." |
| 7 | Id inválido en la URL (`id=abc`) | GET | Mensaje "El proyecto que busca no existe." (no se cae) |

## Repositorio

https://github.com/royzeer/plataforma-ambiental-servlets

---

## English summary

Java web module (Servlets + JSP) to **manage environmental projects**: create, list, search, view, edit and delete, with server-side validation.

**Highlights:** `GET` for queries and forms, `POST` for every change (records are never deleted through a link), JDBC with `PreparedStatement` and the DAO pattern, JSP directives, actions, EL and JSTL, custom error pages and UTF-8 encoding.

**Tech stack:** Java 17 · Jakarta Servlet 6 · JSP + JSTL · JDBC · MySQL 8 · Apache Tomcat 10.1 · Maven · GitHub Actions.

**Run it:** create the database with `database/schema.sql` and `database/datos_prueba.sql`, set your MySQL credentials in `src/main/resources/db.properties`, build the WAR with `mvn package` and deploy it on Tomcat 10.1.
