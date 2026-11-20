# 🍕 MegaPizza

Sistema web para la gestión de ventas en línea (delivery y recojo) y el control de inventario de la pizzería Megapizza. Proyecto del curso **Análisis y diseño de sistemas de información** (Grupo 4, 2026).

## Stack

- **Java 17**, **Jakarta Servlet 6.0 / JSP** (Apache Tomcat 10.1+), paquete `.war`.
- **Maven**, sin Spring Boot, Hibernate ni JPA.
- **MySQL / MariaDB** con **JDBC puro** (`mysql-connector-j`, `PreparedStatement`).
- **JSTL 3.0** en las vistas; CSS/JavaScript vanilla en `static/`.
- Arquitectura en tres capas: `controller`/JSP → `service` → `dao`/`model`.

## Requisitos previos

- **JDK 17** o superior (`java -version`).
- **Maven 3.8+** (`mvn -version`).
- **MySQL 8 / MariaDB** escuchando en `localhost:3306` (o en el host definido por `DB_URL`).
- Opcional: **Apache Tomcat 10.1+** si se despliega el `.war` de forma externa (no hace falta si se usa Cargo, ver más abajo).

## Base de datos

El esquema se llama `BaseDatosMegapizza` y tiene siete tablas, todas con prefijo `tb_`:

| Tabla | Contenido |
|---|---|
| `tb_usuario` | Autenticación y roles (`CLIENTE`, `CAJERO`, `COCINERO`, `ALMACEN`, `ADMINISTRADOR`) |
| `tb_cliente` | Datos de contacto y entrega |
| `tb_producto` | Carta digital (`Pizza`, `Pasta`, `Bebida`, `Combo`) |
| `tb_pedido` | Cabecera, pago digital, estado y total |
| `tb_detalle_pedido` | Productos, cantidades y subtotales |
| `tb_inventario` | Insumos, stock actual y stock mínimo |
| `tb_receta_insumo` | Insumos necesarios por producto |

> ⚠️ `schema.sql` **recrea** las tablas (borra los datos previos). Ejecútalo solo si quieres reiniciar la base de datos de prueba.

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p BaseDatosMegapizza < database/data.sql
```

`data.sql` carga usuarios de los cinco roles, clientes, 12 productos, 14 insumos, 49 recetas y pedidos de ejemplo.

### Usuarios de demostración (contraseñas nuevas)

**Los cinco usuarios comparten una única contraseña: `123456`.**

| Usuario | Contraseña | Rol | Página inicial tras login |
|---|---|---|---|
| cliente | `123456` | CLIENTE | `/catalogo` |
| cajero | `123456` | CAJERO | `/caja` |
| cocina | `123456` | COCINERO | `/cocina` |
| almacen | `123456` | ALMACEN | `/almacen` |
| admin | `123456` | ADMINISTRADOR | `/caja` (puede suplir cualquier rol operativo) |

> Las contraseñas anteriores por rol (`cliente123`, `cajero123`, `cocina123`, `almacen123`, `admin123`) **ya no son válidas**. `data.sql` almacena el mismo hash para los cinco usuarios.

`password_hash` = `SHA-256("megapizza:" + contraseña)`. Para `123456` el valor es:

```
e68128236c61c226df42957cddc131403fe5346ad584bdd9ece20edf28c178e4
```

Si cambias la contraseña de algún usuario, recalcula el hash con ese formato (por ejemplo con `PasswordUtil.hash("nueva_clave")`) y actualiza `tb_usuario.password_hash` (campo `CHAR(64)`).

### Conexión JDBC

Por defecto se usa:

```
jdbc:mysql://localhost:3306/BaseDatosMegapizza?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
```

con usuario `root` y contraseña vacía. Estos valores se pueden sobreescribir con variables de entorno o propiedades del sistema (`-D...`) antes de iniciar Tomcat: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_DRIVER`.

```bash
export DB_URL="jdbc:mysql://localhost:3306/BaseDatosMegapizza"
export DB_USER="root"
export DB_PASSWORD="tu_clave_mysql"
```

## Compilación y ejecución

```bash
mvn clean compile        # solo compila
mvn clean package        # genera target/megapizza.war
```

### Opción A: Tomcat embebido con Cargo (recomendada)

El `pom.xml` incluye el plugin **Cargo** con Tomcat 10.1.34, por lo que no necesitas instalar Tomcat por separado:

```bash
mvn cargo:run
```

El servidor queda corriendo en primer plano en el puerto **8080** con el contexto `/megapizza`. Para detenerlo, pulsa `Ctrl+C`. Si el WAR no está actualizado, ejecuta antes `mvn clean package`.

### Opción B: Tomcat externo

Copia el WAR en `webapps/` de un Tomcat 10.1+ y arranca el servidor:

```bash
cp target/megapizza.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

Para ver los logs: `tail -f $CATALINA_HOME/logs/catalina.out`.

### Acceso

Abre **<http://localhost:8080/megapizza/>**. El `index.jsp` redirige a `/catalogo` (público). Inicia sesión desde `/login.jsp` o con el enlace "Iniciar sesión"; según el rol, `AuthController` redirige a la página inicial de la tabla de usuarios.

### Rutas y control de acceso (RNF01)

| Ruta | Acceso |
|---|---|
| `/`, `/index.jsp`, `/login.jsp`, `/auth`, `/error.jsp`, `/catalogo`, `/static/*` | Público |
| `/pedido`, `/misPedidos.jsp` | CLIENTE (armar el carrito es público; confirmar pedido y ver historial exigen login) |
| `/caja`, `/vistaCaja.jsp` | CAJERO (también ADMINISTRADOR) |
| `/cocina`, `/vistaCocina.jsp` | COCINERO (también ADMINISTRADOR) |
| `/almacen`, `/vistaAlmacen.jsp` | ALMACEN (también ADMINISTRADOR) |
| `/admin` | ADMINISTRADOR |

`AuthFilter` valida el rol guardado en la sesión HTTP; un acceso no autorizado redirige a `/login.jsp` o a `/index.jsp?error=sin-permiso`.

## Flujo de pedidos (CU04/CU05)

1. El cliente arma el carrito y registra el pedido con modalidad de entrega, método digital (Yape/Plin/Tarjeta) y comprobante. Queda en estado **Pendiente** y se calculan subtotales y total dentro de una transacción JDBC.
2. El cajero valida el pago en `/caja`. La confirmación cambia el estado a **Confirmado** y descuenta los insumos de `tb_receta_insumo` con `conn.setAutoCommit(false)`, `commit()` y `rollback()` en la misma transacción. La transición `Pendiente → Confirmado` es condicional, por lo que un pedido no puede descontar stock dos veces.
3. Cocina gestiona **Confirmado → En Preparacion → Listo** y caja registra **Listo → Entregado**; también existe **Rechazado** desde **Pendiente**.
4. Almacén consulta existencias y alertas de stock mínimo, y registra entradas y mermas.

## Documentación

Los artefactos de diseño del proyecto están en `docs/`:

- `AvanceProy2_Grupo4.pdf`
- `ParticipaciónEnClase6_Grupo4.pdf`
