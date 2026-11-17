# HotelUQ — Programación II

Aplicación base funcional en Java 21, Maven y JavaFX, organizada bajo MVC. Incluye dominio, reglas de negocio, repositorios en memoria, pruebas JUnit 5, reportes TXT y diez patrones de diseño.

## Ejecutar
1. Instalar JDK 21 y Maven.
2. Abrir la carpeta `hotel-uq` desde IntelliJ IDEA como proyecto Maven.
3. Ejecutar `mvn clean test`.
4. Ejecutar la interfaz con `mvn javafx:run`.

Usuario de demostración: `admin@hoteluq.com` (rol administrador) y `cliente@hoteluq.com` (rol cliente). La autenticación de esta versión académica es demostrativa; antes de producción se debe incorporar persistencia y hash seguro de contraseñas.

## Arquitectura
- `model`: entidades y enumeraciones.
- `repository`: contratos y repositorios en memoria (DIP).
- `service`: casos de uso y reglas de negocio.
- `pattern/creational`: Factory Method, Builder y Singleton.
- `pattern/structural`: Facade, Adapter, Decorator y Proxy.
- `pattern/behavioral`: Strategy, Observer y State.
- `controller` y `view`: MVC + JavaFX.
- `report`: exportación TXT.

## Patrones implementados
Factory Method crea tipos de habitación; Builder compone reservas; Singleton centraliza configuración; Facade orquesta reservas; Adapter desacopla pasarelas; Decorator agrega servicios; Proxy protege operaciones administrativas; Strategy intercambia políticas de cancelación/promoción; Observer notifica eventos; State controla el ciclo de vida de reservas.

## Reglas académicas adoptadas
- Noche = precio por noche × noches.
- Intervalos de ocupación: [entrada, salida), por lo que una salida y una nueva entrada el mismo día no se superponen.
- Cancelación: más de 72 h = 100%; entre 24 y 72 h inclusive = 80%; menos de 24 h = 0%. Cancelación por hotel = 100%.
- Acumulación: 100 puntos por cada $100.000 COP pagados (redondeo hacia abajo).
- Puntos vencen al cumplirse un año; se consumen primero los que vencen antes.
- Promoción de demostración: 10% si reserva al menos 3 noches.

Los datos se guardan en memoria y se reinician al cerrar la aplicación. Para un despliegue real se debe conectar una base de datos y un proveedor de pagos real.
