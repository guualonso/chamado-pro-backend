package com.chamado.service;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Integração com a API gratuita do CallMeBot para envio de mensagens
 * reais de WhatsApp, sem necessidade de servidor na nuvem.
 *
 * Para funcionar, cada destinatário precisa, uma única vez:
 *  1. Adicionar o número +34 644 84 71 64 aos contatos do WhatsApp;
 *  2. Enviar a mensagem "I allow callmebot to send me messages" para esse contato;
 *  3. Receber de volta uma API Key, que deve ser cadastrada no perfil do usuário
 *     (campo "WhatsApp API Key" na tela de usuários).
 *
 * Documentação: https://www.callmebot.com/blog/free-api-whatsapp-messages/
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${whatsapp.callmebot.enabled:true}")
    private boolean habilitado;

    @Value("${whatsapp.callmebot.base-url:https://api.callmebot.com/whatsapp.php}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envia uma mensagem via WhatsApp para o telefone informado.
     *
     * @param telefone telefone no formato internacional, ex: 5511999999999
     * @param apiKey   API Key do CallMeBot vinculada a esse telefone
     * @param mensagem texto da mensagem
     * @return true se a mensagem foi enviada com sucesso (resposta 2xx da API)
     */
    public boolean enviarMensagem(String telefone, String apiKey, String mensagem) {
        if (!habilitado) {
            log.info("[WhatsApp] Envio desabilitado (whatsapp.callmebot.enabled=false). Mensagem não enviada: {}", mensagem);
            return false;
        }

        if (telefone == null || telefone.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.warn("[WhatsApp] Telefone ou API Key não configurados para o destinatário. Mensagem não enviada.");
            return false;
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("phone", "{phone}")
                    .queryParam("text", "{text}")
                    .queryParam("apikey", "{apikey}")
                    .build()
                    .expand(telefone, mensagem, apiKey)
                    .encode()
                    .toUri();

            ResponseEntity<String> resposta = restTemplate.getForEntity(uri, String.class);

            if (resposta.getStatusCode().is2xxSuccessful()) {
                log.info("[WhatsApp] Mensagem enviada para {}", telefone);
                return true;
            }

            log.warn("[WhatsApp] Falha ao enviar mensagem para {}. Status: {}", telefone, resposta.getStatusCode());
            return false;
        } catch (Exception e) {
            log.error("[WhatsApp] Erro ao enviar mensagem para {}: {}", telefone, e.getMessage());
            return false;
        }
    }
}
