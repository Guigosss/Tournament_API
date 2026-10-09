package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.services.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Override
    public void envoyerMailGagnant(
            String email,
            String nomGagnant,
            String nomTournoi
    ) {
        try {
            // Création du message
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, "UTF-8");

            // Expéditeur
            helper.setFrom(mailFrom);

            // Destinataire
            helper.setTo(email);

            // Objet
            helper.setSubject(
                    "🏆 Félicitations ! Vous avez remporté " + nomTournoi
            );

            // Contenu du mail
            String contenu = """
                    Félicitations %s ! 🏆

                    Vous êtes le grand gagnant du tournoi : %s !

                    Votre performance vous a permis de décrocher
                    la première place du classement.

                    Merci d'avoir participé à notre tournoi !

                    À bientôt pour une nouvelle compétition !

                    L'équipe Tournoi
                    """.formatted(nomGagnant, nomTournoi);

            helper.setText(contenu);

            // Envoi via Brevo
            mailSender.send(message);

        } catch (MessagingException | MailException e) {

            throw new IllegalStateException(
                    "Erreur lors de l'envoi de l'email au gagnant : "
                            + e.getMessage(),
                    e
            );
        }
    }
}
