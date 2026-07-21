Feature: Gestion de citas del taller mecanico

  Scenario: Registrar mantenimiento ligero con otro mecanico disponible
    Given existe un mecanico disponible para mantenimiento ligero
    When agendo un mantenimiento ligero para la placa "MUN-757" el 17 de septiembre de 2026 a las 09:00
    Then la cita queda programada
    And se notifica el agendamiento

  Scenario: Rechazar cita con mecanico ocupado a las 11:00
    Given un mecanico tiene una cita programada el 17 de septiembre de 2026 de 10:00 a 12:00
    When intento agendar mantenimiento ligero con el mismo mecanico a las 11:00
    Then el agendamiento se rechaza por horario ocupado

  Scenario: Aceptar cita cuando la anterior termina a las 12:00
    Given un mecanico tiene una cita programada el 17 de septiembre de 2026 de 10:00 a 12:00
    When intento agendar mantenimiento ligero con el mismo mecanico a las 12:00
    Then la nueva cita queda programada