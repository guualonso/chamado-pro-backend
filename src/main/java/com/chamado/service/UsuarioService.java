package com.chamado.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chamado.dto.ConsentimentoRequest;
import com.chamado.dto.PerfilDTO;
import com.chamado.dto.UsuarioDTO;
import com.chamado.dto.UsuarioResumoDTO;
import com.chamado.exception.RecursoNaoEncontradoException;
import com.chamado.exception.RegraNegocioException;
import com.chamado.model.Usuario;
import com.chamado.model.enums.TipoUsuario;
import com.chamado.repository.ChamadoRepository;
import com.chamado.repository.ComentarioRepository;
import com.chamado.repository.NotificacaoRepository;
import com.chamado.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final int SENHA_MIN = 8;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChamadoRepository chamadoRepository;
    private final ComentarioRepository comentarioRepository;
    private final NotificacaoRepository notificacaoRepository;

    @Value("${app.legal.versao-termos}")
    private String versaoTermosAtual;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          ChamadoRepository chamadoRepository, ComentarioRepository comentarioRepository,
                          NotificacaoRepository notificacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.chamadoRepository = chamadoRepository;
        this.comentarioRepository = comentarioRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    public UsuarioDTO criarUsuario(UsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RegraNegocioException("Já existe um usuário com este e-mail");
        }
        validarSenha(dto.getSenha(), true);

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setTipoUsuario(dto.getTipoUsuario());
        usuario.setNivelTecnico(dto.getTipoUsuario() == TipoUsuario.TECNICO ? dto.getNivelTecnico() : null);
        usuario.setTelefone(vazioParaNulo(dto.getTelefone()));
        usuario.setWhatsappApiKey(vazioParaNulo(dto.getWhatsappApiKey()));

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
     * Lista os técnicos com dados mínimos (id, nome e nível),
     * usado para popular seletores de técnico no escalonamento.
     */
    public List<UsuarioResumoDTO> listarTecnicos() {
        return usuarioRepository.findByTipoUsuario(TipoUsuario.TECNICO)
                .stream()
                .filter(u -> !Boolean.TRUE.equals(u.getAnonimizado()))
                .map(u -> new UsuarioResumoDTO(u.getId(), u.getNome(), u.getNivelTecnico()))
                .toList();
    }

    public UsuarioDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    public UsuarioDTO atualizarUsuario(Long id, UsuarioDTO dto) {
        Usuario usuario = buscarEntidade(id);

        if (!usuario.getEmail().equalsIgnoreCase(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RegraNegocioException("Já existe um usuário com este e-mail");
        }

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setTipoUsuario(dto.getTipoUsuario());
        usuario.setNivelTecnico(dto.getTipoUsuario() == TipoUsuario.TECNICO ? dto.getNivelTecnico() : null);

        String telefone = vazioParaNulo(dto.getTelefone());
        if (telefone == null || !telefone.equals(usuario.getTelefone())) {
            // Telefone mudou/foi removido: o consentimento dado para o número anterior não vale para o novo
            usuario.setConsentimentoWhatsapp(false);
            usuario.setDataConsentimentoWhatsapp(null);
        }
        usuario.setTelefone(telefone);

        if (dto.getWhatsappApiKey() != null && !dto.getWhatsappApiKey().isBlank()) {
            usuario.setWhatsappApiKey(dto.getWhatsappApiKey());
        }

        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            validarSenha(dto.getSenha(), false);
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        Usuario atualizado = usuarioRepository.save(usuario);
        return toDTO(atualizado);
    }

    public void excluirUsuario(Long id, Usuario solicitante) {
        Usuario usuario = buscarEntidade(id);
        if (usuario.getId().equals(solicitante.getId())) {
            throw new RegraNegocioException("Você não pode excluir o próprio usuário por esta tela");
        }
        usuarioRepository.delete(usuario);
    }

    // ------------------------------------------------------------------
    // Dados do próprio titular (LGPD, art. 18)
    // ------------------------------------------------------------------

    public PerfilDTO perfil(Usuario usuario) {
        PerfilDTO dto = new PerfilDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setTipoUsuario(usuario.getTipoUsuario());
        dto.setNivelTecnico(usuario.getNivelTecnico());
        dto.setTelefone(usuario.getTelefone());
        dto.setWhatsappConfigurado(usuario.getTelefone() != null && usuario.getWhatsappApiKey() != null);
        dto.setConsentimentoWhatsapp(Boolean.TRUE.equals(usuario.getConsentimentoWhatsapp()));
        dto.setDataConsentimentoWhatsapp(usuario.getDataConsentimentoWhatsapp());
        dto.setVersaoTermosAceita(usuario.getVersaoTermosAceita());
        dto.setDataAceiteTermos(usuario.getDataAceiteTermos());
        dto.setVersaoTermosAtual(versaoTermosAtual);
        return dto;
    }

    /** Registra o aceite dos termos e o opt-in/opt-out de WhatsApp do próprio usuário. */
    public PerfilDTO registrarConsentimento(Usuario usuario, ConsentimentoRequest request) {
        if (!versaoTermosAtual.equals(request.getVersaoTermos())) {
            throw new RegraNegocioException("A versão dos termos aceita não é a vigente. Recarregue a página.");
        }
        LocalDateTime agora = LocalDateTime.now();
        usuario.setVersaoTermosAceita(versaoTermosAtual);
        usuario.setDataAceiteTermos(agora);

        boolean antes = Boolean.TRUE.equals(usuario.getConsentimentoWhatsapp());
        if (request.isConsentimentoWhatsapp() != antes) {
            usuario.setConsentimentoWhatsapp(request.isConsentimentoWhatsapp());
            usuario.setDataConsentimentoWhatsapp(agora);
        }
        return perfil(usuarioRepository.save(usuario));
    }

    /** Portabilidade/acesso: exporta os dados pessoais do titular em JSON. */
    @Transactional(readOnly = true)
    public Map<String, Object> exportarDados(Usuario usuario) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("geradoEm", LocalDateTime.now());
        dados.put("perfil", perfil(usuario));
        dados.put("chamadosAbertos", chamadoRepository.findByCliente(usuario).stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId());
                    m.put("titulo", c.getTitulo());
                    m.put("descricao", c.getDescricao());
                    m.put("status", c.getStatus());
                    m.put("dataCriacao", c.getDataCriacao());
                    m.put("avaliacao", c.getAvaliacao());
                    m.put("feedback", c.getFeedback());
                    return m;
                }).toList());
        dados.put("comentarios", comentarioRepository.findByAutor(usuario).stream()
                .map(c -> Map.of(
                        "id", c.getId(),
                        "chamadoId", c.getChamado().getId(),
                        "texto", c.getTexto(),
                        "dataCriacao", c.getDataCriacao()))
                .toList());
        dados.put("notificacoes", notificacaoRepository.findByDestinatarioOrderByDataEnvioDesc(usuario).stream()
                .map(n -> Map.of(
                        "mensagem", n.getMensagem(),
                        "canal", n.getCanal(),
                        "dataEnvio", n.getDataEnvio()))
                .toList());
        return dados;
    }

    /**
     * Eliminação a pedido do titular. Os dados pessoais são anonimizados
     * (em vez de apagados) para manter a integridade do histórico dos chamados,
     * que pode precisar ser guardado por obrigação legal/contratual.
     */
    @Transactional
    public void anonimizar(Usuario usuario) {
        if (CurrentUserService.isAdmin(usuario)
                && usuarioRepository.findByTipoUsuario(TipoUsuario.ADMIN).size() <= 1) {
            throw new RegraNegocioException("Não é possível remover o único administrador do sistema");
        }
        notificacaoRepository.deleteAll(notificacaoRepository.findByDestinatarioOrderByDataEnvioDesc(usuario));

        byte[] aleatorio = new byte[24];
        new SecureRandom().nextBytes(aleatorio);

        usuario.setNome("Usuário removido");
        usuario.setEmail("removido-" + usuario.getId() + "@anonimizado.invalid");
        usuario.setSenha(passwordEncoder.encode(Base64.getEncoder().encodeToString(aleatorio)));
        usuario.setTelefone(null);
        usuario.setWhatsappApiKey(null);
        usuario.setConsentimentoWhatsapp(false);
        usuario.setDataConsentimentoWhatsapp(null);
        usuario.setAnonimizado(true);
        usuarioRepository.save(usuario);
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private void validarSenha(String senha, boolean obrigatoria) {
        if (senha == null || senha.isEmpty()) {
            if (obrigatoria) {
                throw new RegraNegocioException("A senha é obrigatória");
            }
            return;
        }
        if (senha.length() < SENHA_MIN) {
            throw new RegraNegocioException("A senha deve ter pelo menos " + SENHA_MIN + " caracteres");
        }
    }

    private static String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setTipoUsuario(usuario.getTipoUsuario());
        dto.setNivelTecnico(usuario.getNivelTecnico());
        dto.setTelefone(usuario.getTelefone());
        dto.setConsentimentoWhatsapp(Boolean.TRUE.equals(usuario.getConsentimentoWhatsapp()));
        return dto;
    }
}
