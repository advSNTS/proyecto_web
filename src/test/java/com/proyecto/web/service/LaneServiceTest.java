package com.proyecto.web.service;

import com.proyecto.web.dto.LaneRequestDTO;
import com.proyecto.web.dto.LaneResponseDTO;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Lane;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.entity.RolXEmpleado;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.LaneRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.RolRepository;
import com.proyecto.web.repository.RolXEmpleadoRepository;
import com.proyecto.web.security.UsuarioPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaneServiceTest {

    @Mock
    private LaneRepository laneRepository;

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private RolXEmpleadoRepository rolXEmpleadoRepository;

    @InjectMocks
    private LaneService laneService;

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listarPorPool_deberiaRetornarSoloLanesDelRolDelEmpleado() {
        UsuarioPrincipal principal = principal(100L, "NIT-001", false);
        autenticar(principal);

        Empresa empresa = Empresa.builder().nit("NIT-001").build();
        Pool pool = Pool.builder().id(10L).empresa(empresa).build();
        Rol rolPermitido = Rol.builder().id(7L).empresa(empresa).build();

        Lane visible = Lane.builder().id(1L).pool(pool).nombre("Visible").rolProceso(rolPermitido).build();

        RolXEmpleado asignacion = RolXEmpleado.builder()
                .empleado(Empleado.builder().id(100L).empresa(empresa).build())
                .rol(rolPermitido)
                .build();

        when(poolRepository.findById(10L))
                .thenReturn(Optional.of(pool));
        when(rolXEmpleadoRepository.findAllByEmpleado_IdAndDeletedFalse(100L))
                .thenReturn(List.of(asignacion));
        when(laneRepository.findAllByPool_IdAndEliminadoFalseAndRolProceso_IdIn(10L, List.of(7L)))
                .thenReturn(List.of(visible));

        List<LaneResponseDTO> result = laneService.listarPorPool(10L);

        assertEquals(1, result.size());
        assertEquals("Visible", result.get(0).getNombre());
        verify(laneRepository).findAllByPool_IdAndEliminadoFalseAndRolProceso_IdIn(10L, List.of(7L));
        verify(laneRepository, never()).findAllByPool_IdAndEliminadoFalse(10L);
    }

    @Test
    void listarPorPool_conAdminGlobal_deberiaRetornarTodasLasLanesDelPool() {
        autenticar(principal(1L, "NIT-001", true));

        Empresa empresa = Empresa.builder().nit("NIT-001").build();
        Pool pool = Pool.builder().id(10L).empresa(empresa).build();
        Lane lane = Lane.builder().id(1L).pool(pool).nombre("Lane").rolProceso(
                Rol.builder().id(7L).empresa(empresa).build()).build();

        when(poolRepository.findById(10L))
                .thenReturn(Optional.of(pool));
        when(laneRepository.findAllByPool_IdAndEliminadoFalse(10L))
                .thenReturn(List.of(lane));

        List<LaneResponseDTO> result = laneService.listarPorPool(10L);

        assertEquals(1, result.size());
        assertEquals("Lane", result.get(0).getNombre());
        verify(laneRepository).findAllByPool_IdAndEliminadoFalse(10L);
    }

    @Test
    void obtener_deberiaLanzarAccessDeniedCuandoLaneNoEsDelEmpleado() {
        autenticar(principal(100L, "NIT-001", false));

        Empresa empresa = Empresa.builder().nit("OTRA-NIT").build();
        Rol rolPermitido = Rol.builder().id(7L).empresa(empresa).build();
        Pool pool = Pool.builder().id(10L).empresa(empresa).build();
        Lane lane = Lane.builder().id(1L).pool(pool).rolProceso(rolPermitido).build();

        when(laneRepository.findById(1L))
                .thenReturn(Optional.of(lane));

        assertThrows(AccessDeniedException.class, () -> laneService.obtener(1L));
    }

    @Test
    void listarTodasPorEmpresa_deberiaRetornarTodasLasLanes() {
        autenticar(principal(1L, "NIT-001", true));
        
        Empresa empresa = Empresa.builder().nit("NIT-001").build();
        Pool pool = Pool.builder().id(10L).empresa(empresa).build();
        Lane lane = Lane.builder().id(1L).pool(pool).nombre("Lane A").build();

        when(poolRepository.findAllByEmpresa_NitAndEliminadoFalse("NIT-001"))
                .thenReturn(List.of(pool));
        when(laneRepository.findAllByPool_Empresa_NitAndEliminadoFalse("NIT-001"))
                .thenReturn(List.of(lane));

        List<LaneResponseDTO> result = laneService.listarTodasPorEmpresa();

        assertEquals(1, result.size());
        assertEquals("Lane A", result.get(0).getNombre());
    }

    @Test
    void listarTodasPorPool_poolInexistente_deberiaLanzarNotFound() {
        autenticar(principal(1L, "NIT-001", true));
        
        when(poolRepository.findById(99L))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> laneService.listarTodasPorPool(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void listarPorEmpresa_sinRolesVisibles_deberiaRetornarListaVacia() {
        autenticar(principal(100L, "NIT-001", false));

        when(rolXEmpleadoRepository.findAllByEmpleado_IdAndDeletedFalse(100L))
                .thenReturn(List.of());

        List<LaneResponseDTO> result = laneService.listarPorEmpresa();

        assertTrue(result.isEmpty());
        verify(laneRepository, never()).findAllByPool_Empresa_NitAndEliminadoFalse("NIT-001");
    }

    @Test
    void crear_deberiaRechazarLaneSinRolProceso() {
        autenticar(principal(100L, "NIT-001", false));

        Empresa empresa = Empresa.builder().nit("NIT-001").build();
        Pool pool = Pool.builder().id(10L).empresa(empresa).build();
        when(poolRepository.findById(10L))
                .thenReturn(Optional.of(pool));

        LaneRequestDTO dto = LaneRequestDTO.builder()
                .poolId(10L)
                .nombre("Nueva lane")
                .rolProcesoId(null)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> laneService.crear(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    private void autenticar(UsuarioPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_READER"))));
    }

    private UsuarioPrincipal principal(Long empleadoId, String nitEmpresa, boolean adminGlobal) {
        return new UsuarioPrincipal(empleadoId, nitEmpresa, adminGlobal, List.of());
    }
}
