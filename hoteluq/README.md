# HotelUQ — Sistema Integral de Gestión Hotelera

Proyecto académico de Programación II desarrollado con **Java 21, JavaFX, Maven y JUnit 5**, con arquitectura MVC y repositorios en memoria.

## Requisitos y ejecución

- JDK 21 y Maven instalados y configurados.
- Abrir la carpeta **hoteluq** (la que contiene `pom.xml`) en IntelliJ IDEA.
- Ejecutar `mvn clean test` para comprobar las pruebas.
- Ejecutar `mvn javafx:run` para iniciar la interfaz gráfica.

## Usuarios de demostración

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | admin@hoteluq.com | admin123 |
| Cliente | cliente@hoteluq.com | cliente123 |

**Atención:** son credenciales exclusivamente de demostración. Los datos y usuarios se almacenan en memoria; al reiniciar la aplicación se pierde el estado. El cliente demo inicia con una recarga de COP $3.000.000.

## Funcionalidades

- Autenticación, registro de clientes y autorización por rol.
- Administración de habitaciones: creación, actualización, activación y desactivación.
- Reservas con control de disponibilidad por fechas y estados pendiente, confirmada y cancelada.
- Servicios adicionales configurables (desayuno, parqueadero, spa y transporte).
- Tarjeta virtual: recargas, cobros, reembolsos, consulta de movimientos y bloqueo/activación.
- Fidelización: 1 punto por cada COP $10.000 pagados; 1 punto equivale a COP $1.000 de descuento; pago mínimo de COP $1.000; vencimiento anual.
- Cancelación de reservas y tratamiento de reembolsos y puntos.
- Reportes administrativos de reservas, ingresos estimados por reservas confirmadas y ocupación; exportación TXT.
- Interfaz JavaFX y pruebas automatizadas JUnit 5.

## Reglas de cancelación

La política estándar devuelve el **100 %** cuando faltan más de 72 horas para la entrada, el **80 %** entre 24 y 72 horas (inclusive), y **0 %** cuando faltan menos de 24 horas. La cancelación por el hotel devuelve el 100 % del valor pagado. La implementación calcula horas completas mediante `Duration.toHours()`.

## Estructura del código

| Paquete | Responsabilidad |
| --- | --- |
| `app` | Inicio de la aplicación |
| `model` | Entidades, puntos y billetera |
| `repository` | Repositorios en memoria |
| `service` | Reglas de negocio, catálogo y reportes |
| `controller` | Coordinación MVC |
| `view` | Pantallas JavaFX |
| `pattern/creational` | Factory Method, Builder, Singleton |
| `pattern/structural` | Facade, Adapter, Decorator, Proxy |
| `pattern/behavioral` | Strategy, Observer, State |
| `security` | Hash de contraseñas |

## Patrones de diseño

Consulte `docs/PATRONES_Y_ARQUITECTURA.md` y los diagramas PlantUML de `docs/uml/`.

## Pruebas y entrega

Ejecute `mvn clean test` y verifique `BUILD SUCCESS`. En la ejecución reportada por el estudiante del sexto avance pasaron **56 pruebas**, pero **esta copia final no ha sido reejecutada en este entorno**. Valide también manualmente el inicio de sesión, la reserva como cliente, los reportes como administrador y la exportación TXT.

## Limitaciones

Aplicación académica: no usa base de datos ni pasarela de pago externa. Los reportes de ingresos se basan en los totales de reservas confirmadas, no en contabilidad bancaria. No se garantiza transaccionalidad completa ante fallos intermedios en pagos, cancelaciones y puntos. No utilizar con dinero real ni datos sensibles de producción.
