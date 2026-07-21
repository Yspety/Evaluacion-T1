package edu.pe.cibertec.taller.servicio;

import edu.pe.cibertec.taller.excepcion.EspecialidadIncorrectaException;
import edu.pe.cibertec.taller.excepcion.MecanicoNoEncontradoException;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.Mecanico;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ServicioCitasImplTest {

	@Mock
	private RepositorioMecanicos repositorioMecanicos;

	@Mock
	private RepositorioCitas repositorioCitas;

	@Mock
	private ProveedorFechaHora proveedorFechaHora;

	@Mock
	private ServicioNotificaciones servicioNotificaciones;

	private ServicioCitasImpl servicioCitas;

	@BeforeEach
	void inicializar() {
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);
		// TODO: crear aqui los datos comunes que necesiten los tests
	}

	@Test
	@DisplayName("Agendar una cita valida la guarda, notifica y la retorna en estado PROGRAMADA")
	void agendarCitaExitosa() {
		// Arrange
		// TODO

        String placaZafiro = "MUN-757";
        Long idMecanico = 1L;

        LocalDateTime fechaActual =
                LocalDateTime.of(2026, 9, 16, 8, 0);

        LocalDateTime fechaCita =
                LocalDateTime.of(2026, 9, 17, 10, 0);

        Mecanico mecanico = new Mecanico(
                idMecanico,
                "Billy Muñoz",
                TipoServicio.CAMBIO_ACEITE
        );

        Cita citaGuardada = new Cita(
                1L,
                mecanico,
                placaZafiro,
                TipoServicio.CAMBIO_ACEITE,
                fechaCita,
                1,
                EstadoCita.PROGRAMADA
        );

        when(repositorioMecanicos.findById(idMecanico))
                .thenReturn(Optional.of(mecanico));

        when(proveedorFechaHora.ahora())
                .thenReturn(fechaActual);

        when(repositorioCitas.findByMecanicoIdAndEstado(
                idMecanico,
                EstadoCita.PROGRAMADA
        )).thenReturn(Collections.emptyList());

        when(repositorioCitas.save(any(Cita.class)))
                .thenReturn(citaGuardada);


		// Act
		// TODO


        Cita resultado = servicioCitas.agendarCita(
                idMecanico,
                placaZafiro,
                TipoServicio.CAMBIO_ACEITE,
                fechaCita
        );




		// Assert
		// TODO: verificar estado, duracion, save y notificacion

        assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
        assertEquals(1, resultado.getDuracionHoras());

        verify(repositorioCitas, times(1))
                .save(any(Cita.class));

        verify(servicioNotificaciones, times(1))
                .notificarCitaAgendada(citaGuardada);

	}

	@Test
	@DisplayName("Agendar con un mecanico inexistente lanza MecanicoNoEncontradoException")
	void agendarConMecanicoInexistente() {
		// Arrange
		// TODO

        String placaZafiro = "MUN-757";
        Long idMecanico = 99L;

        LocalDateTime fechaCita =
                LocalDateTime.of(2026, 9, 17, 10, 0);

        when(repositorioMecanicos.findById(idMecanico))
                .thenReturn(Optional.empty());



		// Act y Assert

        // TODO

        assertThrows(
                MecanicoNoEncontradoException.class,
                () -> servicioCitas.agendarCita(
                        idMecanico,
                        placaZafiro,
                        TipoServicio.CAMBIO_ACEITE,
                        fechaCita
                )
        );

        verify(repositorioCitas, never())
                .save(any(Cita.class));


	}

	@Test
	@DisplayName("Agendar cuando la especialidad no coincide lanza EspecialidadIncorrectaException")
	void agendarConEspecialidadIncorrecta() {
		// Arrange
		// TODO

        String placaZafiro = "MUN-757";
        Long idMecanico = 1L;

        LocalDateTime fechaCita =
                LocalDateTime.of(2026, 9, 17, 10, 0);

        Mecanico mecanico = new Mecanico(
                idMecanico,
                "Billy Muñoz",
                TipoServicio.CAMBIO_ACEITE
        );

        when(repositorioMecanicos.findById(idMecanico))
                .thenReturn(Optional.of(mecanico));




		// Act y Assert
		// TODO

        assertThrows(
                EspecialidadIncorrectaException.class,
                () -> servicioCitas.agendarCita(
                        idMecanico,
                        placaZafiro,
                        TipoServicio.REPARACION_MOTOR,
                        fechaCita
                )
        );

        verify(repositorioCitas, never())
                .save(any(Cita.class));




	}

	@Test
	@DisplayName("Un servicio pesado a las 15:00 se rechaza con HorarioNoPermitidoException")
	void agendarServicioPesadoEnLaTarde() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Un servicio pesado a las 09:00 se acepta y se guarda")
	void agendarServicioPesadoEnLaManana() {
		// Arrange
		// TODO

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Agendar en una fecha del pasado lanza FechaInvalidaException")
	void agendarConFechaEnElPasado() {
		// Arrange
		// TODO: recuerden mockear proveedorFechaHora.ahora()

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Agendar sobre una cita ya programada se rechaza con HorarioOcupadoException")
	void agendarConSuperposicion() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Una cita que empieza justo cuando termina otra se acepta")
	void agendarCitaContigua() {
		// Arrange
		// TODO: una cita existente que termina a las 10:00 y la nueva que empieza a las 10:00

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Cancelar con 24 horas o mas de anticipacion no genera penalidad")
	void cancelarConAnticipacionSuficiente() {
		// Arrange
		// TODO

		// Act
		// TODO

		// Assert
		// TODO: penalidad 0, estado CANCELADA, notificacion
	}

	@Test
	@DisplayName("Cancelar con menos de 24 horas aplica una penalidad de 50.00")
	void cancelarConAvisoTardio() {
		// Arrange
		// TODO

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Cancelar una cita inexistente lanza CitaNoEncontradaException")
	void cancelarCitaInexistente() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Cancelar una cita que ya fue cancelada lanza CitaNoCancelableException")
	void cancelarCitaYaCancelada() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Buscar mecanico disponible retorna el primero sin citas superpuestas")
	void buscarMecanicoDisponibleRetornaPrimeroLibre() {
		// Arrange
		// TODO: dos mecanicos de la misma especialidad, el primero ocupado

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Buscar mecanico cuando ninguno esta libre lanza SinDisponibilidadException")
	void buscarMecanicoSinDisponibilidad() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}
}
