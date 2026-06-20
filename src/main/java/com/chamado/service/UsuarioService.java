package com.chamado.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.chamado.dto.UsuarioDTO;
import com.chamado.model.Usuario;
import com.chamado.model.enums.TipoUsuario;
import com.chamado.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;


    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioDTO criarUsuario(UsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Já existe um usuário com este e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setTipoUsuario(dto.getTipoUsuario());
        usuario.setNivelTecnico(dto.getNivelTecnico());
        usuario.setTelefone(dto.getTelefone());
        usuario.setWhatsappApiKey(dto.getWhatsappApiKey());

        Usuario salvo = usuarioRepository.save(usuario);
        return toDTO(salvo);
    }


    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Lista usuários filtrando por tipo (ex: todos os TECNICO),
     * usado para popular seletores de técnico no escalonamento.
     */
    public List<UsuarioDTO> listarPorTipo(TipoUsuario tipo) {
        return usuarioRepository.findByTipoUsuario(tipo)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toDTO(usuario);
    }

    public UsuarioDTO atualizarUsuario(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setTipoUsuario(dto.getTipoUsuario());
        usuario.setNivelTecnico(dto.getNivelTecnico());
        usuario.setTelefone(dto.getTelefone());

        if (dto.getWhatsappApiKey() != null && !dto.getWhatsappApiKey().isBlank()) {
            usuario.setWhatsappApiKey(dto.getWhatsappApiKey());
        }

        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        Usuario atualizado = usuarioRepository.save(usuario);
        return toDTO(atualizado);
    }

    public void excluirUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuarioRepository.delete(usuario);
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setSenha(usuario.getSenha());
        dto.setTipoUsuario(usuario.getTipoUsuario());
        dto.setNivelTecnico(usuario.getNivelTecnico());
        dto.setTelefone(usuario.getTelefone());
        return dto;
    }
}
