# BACKLOG — bluke

proyecto:: bluke
estado:: activo
actualizado:: 2026-11-01

Sale de la revisión de código sobre `bcf4f14`. Las líneas citadas son de ese commit y los bugs se trazaron leyendo, sin ejecutarlos. `TPV` es `app/src/main/java/dev/arnv/bluke/ui/TouchpadView.kt` y `BKM` es `.../bluetooth/BluetoothKeyboardManager.kt`.

## Abierto

### Upstream
- [ ] **Decidir el PR a upstream de `feat/modos-del-toggle`** (2026-10-31, Chabe #5723) prioridad:: media
	La rama sale de `upstream/main` y es un solo PR. Si hay que dividirla, y si se abre, lo decide Enrique.

### Errores
- [ ] **Salto del cursor al terminar el scroll a dos dedos** (2026-10-31, revisión de código) prioridad:: alta
	La rama de dos dedos no actualiza `lastPointX/Y` ni `lastActivePointerId` (TPV:902-923). Si el primer dedo queda abajo, TPV:845-853 envía todo su recorrido como movimiento.
- [ ] **Doble tap rápido convertido en un solo clic** (2026-10-31, revisión de código) prioridad:: alta
	`move()` no tiene tolerancia (TPV:861, `ui/TouchpadTapDragGesture.kt:58-63`). Con un temblor mínimo en el segundo toque, se cancela el primer clic y el host recibe uno solo. La prueba sólo cubre el caso sin movimiento.
- [ ] **Sin reconexión automática tras una caída con la app abierta** (2026-10-31, revisión de código) prioridad:: alta
	BKM:1254 usa `scheduleReconnect=false` y `automaticReconnectAttempted` sólo se limpia al volver a primer plano (BKM:449). Si el host estaba apagado al abrir, hay un solo intento (BKM:1121). Arreglo: `scheduleReconnect=true` con reintentos espaciados.
- [ ] **Clic oculto en el 18 % inferior en modo Clickpad** (2026-10-31, revisión de código) prioridad:: alta
	Empezar ahí presiona el botón izquierdo (TPV:882-898) sin guía visual (TPV:1020). A oscuras, el pulgar arrastra sin querer. Además `buttonMode` no persiste (TPV:75).
- [ ] **Un host entrante desplaza la reconexión automática** (2026-10-31, revisión de código) prioridad:: media
	La reconexión cede ante cualquier host emparejado y ese host pasa a ser el último guardado (BKM:1139-1150). Mientras tanto, emparejar sólo la tele.
- [ ] **Proxy HID sin cerrar al apagar Bluetooth** (2026-10-31, revisión de código) prioridad:: baja
	BKM:516 suelta el proxy y el único cierre está en BKM:1804. El doble enlace al volver a prender es inferido.
- [ ] **Teclas equivocadas en el numpad del touchpad** (2026-10-31, revisión de código) prioridad:: baja
	`*`, `%` y `+` envían 8, 5 y = porque no se manda Shift (TPV:598-604).
- [ ] **GET_REPORT sin respuesta** (2026-10-31, revisión de código) prioridad:: baja
	`onGetReport` sólo registra el evento (BKM:1292). Sin reportes Feature, el impacto debería ser bajo.

### Mejoras para la cama
- [ ] **Probar en la tele con Moonlight** (2026-10-31, revisión de código) prioridad:: alta
	Confirmar emparejamiento, rueda y clics. Las cuatro colecciones podrían formar un solo dispositivo de entrada y Moonlight ver un mando fantasma; es inferido y sólo se confirma en la tele.
- [ ] **Scroll en dp, más lento por omisión y con inversión** (2026-10-31, revisión de código) prioridad:: alta
	Hoy es 1 muesca cada 16.7 px, unos 1.1 mm en FHD+ (estimado), y depende de la resolución de pantalla (TPV:911). No hay opción de invertir.
- [ ] **Pantalla encendida, brillo mínimo y negro puro en el touchpad** (2026-10-31, revisión de código) prioridad:: alta
	No hay `keepScreenOn` ni wakelock, y `onStop` suelta las entradas (`MainActivity.kt:122`). El fondo es `#1E1E1E` (TPV:464).
- [ ] **Aceleración del puntero** (2026-10-31, revisión de código) prioridad:: media
	TPV:852 es lineal. Hace falta precisión lenta y recorrido rápido para cruzar una pantalla de 65".
- [ ] **Modo vertical a una mano** (2026-10-31, revisión de código) prioridad:: media
	El modo activo fuerza horizontal (`ui/HomeScreen.kt:436`).
- [ ] **Quitar el gamepad del descriptor si aparece el mando fantasma** (2026-10-31, revisión de código) prioridad:: baja
	Depende de la prueba en la tele. Exige volver a emparejar.
- [ ] **Rueda horizontal con AC Pan** (2026-10-31, revisión de código) prioridad:: baja
	Exige reporte de 5 bytes y volver a emparejar. `docs/USAGE.md:60` ya promete scroll horizontal, que no existe.
- [ ] **Botones de volumen con la pantalla apagada** (2026-10-31, revisión de código) prioridad:: baja
	Hoy sólo se atienden en la actividad (`MainActivity.kt:160`). Haría falta `MediaSession`.

## Hecho

## Descartado
