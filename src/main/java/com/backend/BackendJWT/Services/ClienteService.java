package com.backend.BackendJWT.Services;

import com.backend.BackendJWT.Models.Auth.AuthResponse;
import com.backend.BackendJWT.Models.Auth.Cliente;
import com.backend.BackendJWT.Models.Auth.Medidor;
import com.backend.BackendJWT.Repositories.Auth.ClienteRepository;
import com.backend.BackendJWT.Repositories.Auth.MedidorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente getClienteByRut(String rut) {
        System.out.println("cliente rut: "+rut);
        return clienteRepository.findByRut(rut)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Autowired
    private MedidorRepository medidorRepository;

    public AuthResponse registrarMedidor(Medidor medidor, Cliente cliente) {
        medidor.setCliente(cliente);
        medidorRepository.save(medidor);
        return new AuthResponse(true, "Medidor registrado con exito");
    }
}