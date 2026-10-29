# Alarma TV

App Android nativa, en Java, independiente de CleanYourTV, para probar alarmas con sonido en un Philips 55PUD7906/44 (Android TV 11). No es una aplicación privilegiada del sistema. No requiere Internet, root ni modificar el launcher o firmware.

## Funciones

- Una alarma configurable, con hora de 24 horas y selección de días. Ningún día seleccionado significa una sola vez.
- Tono propio incluido y selección de tonos de alarma del firmware, si el TV ofrece selector.
- Volumen de alarma temporal, restaurado al detener; no modifica el volumen multimedia deliberadamente. Algunos televisores tienen volumen fijo o rutas de audio propias: se debe comprobar la salida real.
- Botones grandes para el control remoto, prueba inmediata, detener y posponer 10 minutos. Límite de sonido de cinco minutos.
- `AlarmManager.setAlarmClock`, servicio en primer plano, audio con `USAGE_ALARM`, notificación de pantalla completa y actividad con `setTurnScreenOn`. Reprogramación tras reiniciar o cambiar hora/zona horaria; la app debe abrirse y guardarse al menos una vez.
- El archivo WAV es sintetizado por este proyecto; no depende de que haya tonos preinstalados.

## Compilar e instalar desde Windows

JDK 17 y Android SDK platform 34 / build-tools 34.0.0. Se reutilizan los componentes ya preparados en el PC, no hace falta Android Studio. Mantener esta carpeta y su clave de depuración de Windows para futuras actualizaciones.

Desde PowerShell dentro de este proyecto:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\install-windows.ps1 -TvHost 192.168.1.40
```

El script compila y ejecuta lint antes de instalar. No desactiva aplicaciones, no modifica fondos y no reconfigura el launcher. No sortear errores de verificación de APK ni aceptar avisos desconocidos en la TV.

## Validación en el televisor (pendiente)

1. Abrir Alarma TV desde el launcher. Elegir volumen bajo, usar tono propio y Probar sonido ahora. Confirmar sonido por los altavoces usados normalmente. Detener y comprobar que el volumen de alarma se restaura.
2. Activar una alarma para dentro de dos minutos, sin días marcados, pulsar Guardar y comprobar la próxima fecha. Volver a Inicio dejando la TV encendida. Confirmar que suena y se puede detener con el control.
3. Repetir desde reposo al apagar con el control. No desenchufar el TV. Comprobar por separado encendido del panel, sonido y acceso a Detener/Posponer.
4. Probar Posponer y comprobar la próxima fecha. Probar una alarma después de reiniciar el TV.
5. Comprobar volumen con altavoces internos y, si se usan, barra HDMI/Bluetooth. Conservar resultado y errores visibles para diagnosticar el firmware.
6. Confirmar Ambilight, voz del control, Chromecast, HDMI y APKs, que esta app no modifica.
7. Tras instalar y probar, cerrar la depuración de red/USB. La app no usa ADB ni necesita el PC para sonar.

## Límites

No se ha verificado audio, navegación, notificaciones ni encendido en un dispositivo real. Compilar y pasar lint no demuestra estas funciones. En Android TV el firmware puede suprimir la pantalla completa, el audio en reposo o el encendido del panel; `RTC_WAKEUP`/AlarmClock despierta CPU pero no garantiza despertar todo el hardware. No se promete resolverlo ni se atribuye el fallo a Android 11 sin pruebas. Una TV desenchufada no puede sonar. Forzar detención desde Android invalida alarmas; abrir y guardar de nuevo. No usar como único despertador hasta verificar varios ciclos de reposo y reinicio.

Android 12+ requiere autorización de alarmas exactas; se incluye un botón para solicitarla. El objetivo probado aquí es compilar para Android TV 11; otras versiones y tiendas no han sido validadas.

## Comprobaciones de desarrollo

```sh
./gradlew --no-daemon :app:assembleDebug :app:lintDebug
javac -d /tmp/tv-alarm-tests app/src/main/java/com/flexxnow/tvalarm/NextAlarm.java tests/NextAlarmTest.java
java -cp /tmp/tv-alarm-tests com.flexxnow.tvalarm.NextAlarmTest
```

Los 134 casos de horarios verifican el día actual, hora ya pasada, alarma única, semana siguiente y las 128 máscaras de repetición. No sustituyen la prueba de AlarmManager o reproducción real en Android.

## Quitar la app

En Ajustes del televisor → Aplicaciones → Alarma TV → Desinstalar. Android cancela sus alarmas. Detener primero cualquier sonido activo. El resto de la TV queda intacto.

## Estado de esta versión

Verificado en el entorno de desarrollo: compilación `assembleDebug`, `lintDebug` (0 errores; 7 avisos sobre traducción de textos y reglas de backup), 134 casos de cálculo de horarios y firma APK v2. Pendiente: instalar en el Philips y validar sonido, encendido desde reposo, volumen, interfaz con mando y posponer. No se ha instalado nada en la TV desde este entorno.

### Versión 1.1: corrección visual para TV

Los botones usan altura mínima en dp y altura de contenido, en lugar de 56 píxeles fijos que recortaban el texto en el Philips. También se adaptaron márgenes, controles de días y volumen; el foco del mando se destaca con un borde turquesa. La pantalla se desplaza al navegar si no cabe todo. Comprobar visualmente esta actualización en el televisor: hora, tonos, Guardar, prueba de sonido y Detener deben leerse completos, y los botones de la pantalla de alarma también.
