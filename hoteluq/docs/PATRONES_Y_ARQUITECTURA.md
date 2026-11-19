# Arquitectura y patrones de diseño de HotelUQ

## Arquitectura MVC

- **Vista:** `HotelView` (JavaFX) recoge eventos del usuario y muestra resultados.
- **Controlador:** `HotelController` valida acceso y coordina servicios.
- **Modelo y servicios:** `Room`, `Reservation`, `User`, `Wallet`, `PointsAccount`, `RoomService`, `ReservationService`, `LoyaltyService`, `ReportService` y otros.
- **Persistencia:** implementaciones de `Repository` en memoria.

## Patrones identificados en el código

| Patrón | Clases representativas | Función |
| --- | --- | --- |
| Factory Method | `RoomCreator`, `StandardRoomCreator`, `FamilyRoomCreator`, `SuiteRoomCreator` | Creación especializada de habitaciones |
| Builder | `ReservationBuilder` | Construcción progresiva de reservas |
| Singleton | `HotelConfig` | Configuración compartida |
| Facade | `ReservationFacade` | Simplificación del flujo de reservas |
| Adapter | `WalletPaymentAdapter`, `PaymentGateway` | Adaptación de la billetera a la interfaz de pagos |
| Decorator | `ServiceDecorator`, `BreakfastDecorator` | Composición de servicios |
| Proxy | `AdminOperationsProxy` | Control de operaciones administrativas |
| Strategy | `CancellationPolicy`, `StandardCancellationPolicy`, `HotelCancellationPolicy`, `PromotionStrategy` | Políticas intercambiables |
| Observer | `ReservationObserver`, `EmailNotificationObserver` | Notificaciones sobre cambios de reserva (simuladas) |
| State | `ReservationState`, `PendingState`, `ConfirmedState`, `CancelledState` | Transiciones de estado |

## Flujo de reserva

1. El cliente inicia sesión.
2. El controlador solicita una reserva a `ReservationFacade`.
3. Se comprueban disponibilidad y fechas.
4. Se calcula el total de habitación y servicios.
5. `ReservationService` procesa el pago por `PaymentGateway` y confirma la reserva.
6. Se acreditan puntos y se notifican observadores.
7. Una cancelación utiliza `CancellationPolicy` y ajusta el reembolso y puntos según la implementación.

## Diagramas

Los archivos `.puml` en `docs/uml` pueden abrirse con una extensión PlantUML o renderizarse en cualquier herramienta compatible. Son diagramas simplificados, orientados a explicar el diseño; no sustituyen un UML generado automáticamente de todos los miembros.

## Consideraciones para producción

Persistencia en base de datos, operaciones transaccionales, validación exhaustiva, controles de acceso adicionales, auditoría y pruebas de integración con pagos reales.
