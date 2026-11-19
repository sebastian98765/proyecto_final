# Sexto avance - HotelUQ

Se incorporó un catálogo editable de servicios adicionales (desayuno, parqueadero, spa y transporte), selección de servicios en las reservas demo, redención de puntos desde la interfaz, reportes de reservas e ingresos y reporte de ocupación por noches. El administrador puede crear o actualizar servicios y exportar el reporte generado a TXT.

## Pruebas

En IntelliJ, desde la carpeta `hoteluq`, ejecutar:

```
mvn clean test
mvn javafx:run
```

Credenciales demo: `admin@hoteluq.com` / `admin123` y `cliente@hoteluq.com` / `cliente123`.

## Alcance

El catálogo, las reservas y los reportes se mantienen en memoria; no hay persistencia en base de datos. Los ingresos reflejan reservas actualmente confirmadas, no una contabilidad histórica de pagos y reembolsos. La interfaz de reservas sigue limitada al cliente de demostración. La gestión de puntos del quinto avance no se ha rediseñado en esta entrega.
