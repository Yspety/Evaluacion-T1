package edu.pe.cibertec.taller.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.pe.cibertec.taller.excepcion.HorarioOcupadoException;
import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.modelo.Mecanico;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GestionCitasSteps {

    private RepositorioMecanicos repositorioMecanicos;
    private RepositorioCitas repositorioCitas;
    private ProveedorFechaHora proveedorFechaHora;
    private ServicioNotificaciones servicioNotificaciones;
    private ServicioCitasImpl servicioCitas;

    private Mecanico mecanico;
    private Cita citaExistente;
    private Cita resultado;
    private HorarioOcupadoException excepcionHorarioOcupado;

    @Before
    public void inicializar() {
        repositorioMecanicos = mock(RepositorioMecanicos.class);
        repositorioCitas = mock(RepositorioCitas.class);
        proveedorFechaHora = mock(ProveedorFechaHora.class);
        servicioNotificaciones = mock(ServicioNotificaciones.class);

        servicioCitas = new ServicioCitasImpl(
                repositorioMecanicos,
                repositorioCitas,
                proveedorFechaHora,
                servicioNotificaciones
        );

        when(proveedorFechaHora.ahora())
                .thenReturn(LocalDateTime.of(2026, 9, 16, 8, 0));
    }

    @Given("existe un mecanico disponible para mantenimiento ligero")
    public void existeUnMecanicoDisponibleParaMantenimientoLigero() {
        // Arrange
        String nombreZafiro = "Billy Muñoz";

        mecanico = new Mecanico(
                2L,
                nombreZafiro,
                TipoServicio.MANTENIMIENTO_LIGERO
        );

        when(repositorioMecanicos.findById(2L))
                .thenReturn(Optional.of(mecanico));

        when(repositorioCitas.findByMecanicoIdAndEstado(
                2L,
                EstadoCita.PROGRAMADA
        )).thenReturn(Collections.emptyList());
    }

    @When("agendo un mantenimiento ligero para la placa {string} el 17 de septiembre de 2026 a las 09:00")
    public void agendoUnMantenimientoLigeroParaLaPlaca(String placa) {
        // Arrange
        String placaZafiro = placa;

        LocalDateTime fechaCita =
                LocalDateTime.of(2026, 9, 17, 9, 0);

        Cita citaGuardada = new Cita(
                20L,
                mecanico,
                placaZafiro,
                TipoServicio.MANTENIMIENTO_LIGERO,
                fechaCita,
                2,
                EstadoCita.PROGRAMADA
        );

        when(repositorioCitas.save(any(Cita.class)))
                .thenReturn(citaGuardada);

        // Act
        resultado = servicioCitas.agendarCita(
                2L,
                placaZafiro,
                TipoServicio.MANTENIMIENTO_LIGERO,
                fechaCita
        );
    }

    @Then("la cita queda programada")
    public void laCitaQuedaProgramada() {
        // Assert
        String estadoZafiro = "PROGRAMADA";

        assertEquals(
                estadoZafiro,
                resultado.getEstado().name()
        );

        assertEquals(
                2,
                resultado.getDuracionHoras()
        );

        verify(repositorioCitas, times(1))
                .save(any(Cita.class));
    }

    @And("se notifica el agendamiento")
    public void seNotificaElAgendamiento() {
        // Assert
        String comprobacionZafiro = "notificacion";

        assertEquals(
                "notificacion",
                comprobacionZafiro
        );

        verify(servicioNotificaciones, times(1))
                .notificarCitaAgendada(resultado);
    }

    @Given("un mecanico tiene una cita programada el 17 de septiembre de 2026 de 10:00 a 12:00")
    public void unMecanicoTieneUnaCitaProgramadaDeDiezADoce() {
        // Arrange
        String nombreZafiro = "Billy Muñoz";

        mecanico = new Mecanico(
                1L,
                nombreZafiro,
                TipoServicio.MANTENIMIENTO_LIGERO
        );

        citaExistente = new Cita(
                30L,
                mecanico,
                "ABC-123",
                TipoServicio.MANTENIMIENTO_LIGERO,
                LocalDateTime.of(2026, 9, 17, 10, 0),
                2,
                EstadoCita.PROGRAMADA
        );

        when(repositorioMecanicos.findById(1L))
                .thenReturn(Optional.of(mecanico));

        when(repositorioCitas.findByMecanicoIdAndEstado(
                1L,
                EstadoCita.PROGRAMADA
        )).thenReturn(List.of(citaExistente));
    }

    @When("intento agendar mantenimiento ligero con el mismo mecanico a las 11:00")
    public void intentoAgendarConElMismoMecanicoALasOnce() {
        // Arrange
        String placaZafiro = "MUN-757";

        // Act
        excepcionHorarioOcupado = assertThrows(
                HorarioOcupadoException.class,
                () -> servicioCitas.agendarCita(
                        1L,
                        placaZafiro,
                        TipoServicio.MANTENIMIENTO_LIGERO,
                        LocalDateTime.of(2026, 9, 17, 11, 0)
                )
        );
    }

    @Then("el agendamiento se rechaza por horario ocupado")
    public void elAgendamientoSeRechazaPorHorarioOcupado() {
        // Assert
        String mensajeZafiro =
                "El mecanico ya tiene una cita en ese horario";

        assertEquals(
                mensajeZafiro,
                excepcionHorarioOcupado.getMessage()
        );
    }

    @When("intento agendar mantenimiento ligero con el mismo mecanico a las 12:00")
    public void intentoAgendarConElMismoMecanicoALasDoce() {
        // Arrange
        String placaZafiro = "MUN-757";

        LocalDateTime nuevaFecha =
                LocalDateTime.of(2026, 9, 17, 12, 0);

        Cita citaGuardada = new Cita(
                31L,
                mecanico,
                placaZafiro,
                TipoServicio.MANTENIMIENTO_LIGERO,
                nuevaFecha,
                2,
                EstadoCita.PROGRAMADA
        );

        when(repositorioCitas.save(any(Cita.class)))
                .thenReturn(citaGuardada);

        // Act
        resultado = servicioCitas.agendarCita(
                1L,
                placaZafiro,
                TipoServicio.MANTENIMIENTO_LIGERO,
                nuevaFecha
        );
    }

    @Then("la nueva cita queda programada")
    public void laNuevaCitaQuedaProgramada() {
        // Assert
        String estadoZafiro = "PROGRAMADA";

        assertEquals(
                estadoZafiro,
                resultado.getEstado().name()
        );

        verify(repositorioCitas, times(1))
                .save(any(Cita.class));

        verify(servicioNotificaciones, times(1))
                .notificarCitaAgendada(resultado);
    }
}