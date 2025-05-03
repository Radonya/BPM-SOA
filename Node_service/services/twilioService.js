require('dotenv').config();
const twilio = require('twilio');


const client = twilio(
    process.env.TWILIO_ACCOUNT_SID,
    process.env.TWILIO_AUTH_TOKEN
);


const twilioService = {

    sendWhatsAppMessage: async (to, message) => {
        try {
            
            const fromWhatsApp = `whatsapp:${process.env.TWILIO_WHATSAPP_NUMBER}`;
            const toWhatsApp = `whatsapp:${to}`;
            
           
            const result = await client.messages.create({
                body: message,
                from: fromWhatsApp,
                to: toWhatsApp
            });
            
            console.log(`Message WhatsApp envoyé avec succès, SID: ${result.sid}`);
            return { success: true, sid: result.sid };
        } catch (error) {
            console.error(`Erreur lors de l'envoi du message WhatsApp: ${error.message}`);
            return { success: false, error: error.message };
        }
    },
    
    
    
    notifyEmployeAction: async (employeInfo, action, phoneNumber) => {
        try {
            let messageContent;
            
            switch (action) {
                case "CREATION":
                    messageContent = `Cher(e) ${employeInfo.prenom} ${employeInfo.nom},

Nous avons le plaisir de vous informer que votre compte employé a été créé avec succès dans notre système.

Votre numéro d'employé: ${employeInfo.numeroEmploye}
Date d'enregistrement: ${new Date().toLocaleString('fr-FR')}

Bienvenue dans l'équipe  !`;
                    break;
                case "MODIFICATION":
                    messageContent = `Cher(e) ${employeInfo.prenom} ${employeInfo.nom},

Nous vous informons que vos informations personnelles ont été mises à jour dans notre système.

Votre numéro d'employé: ${employeInfo.numeroEmploye}
Date de modification: ${new Date().toLocaleString('fr-FR')}

Si vous n'avez pas demandé cette modification, veuillez contacter immédiatement votre responsable RH.`;
                    break;
                case "SUPPRESSION":
                    messageContent = `Cher(e) ${employeInfo.prenom} ${employeInfo.nom},

Nous vous informons que votre compte employé (n°${employeInfo.numeroEmploye}) a été désactivé dans notre système.

Pour toute question, veuillez contacter le service RH.

Cordialement,
L'équipe `;
                    break;
                default:
                    messageContent = `Cher(e) ${employeInfo.prenom} ${employeInfo.nom},

Nous vous informons qu'une action "${action}" a été effectuée concernant votre compte employé.

Votre numéro d'employé: ${employeInfo.numeroEmploye}
Date de l'action: ${new Date().toLocaleString('fr-FR')}

Pour plus d'informations, veuillez consulter votre espace personnel.`;
            }
            
            const message = `

${messageContent}
            `.trim();
            
            return await twilioService.sendWhatsAppMessage(phoneNumber, message);
        } catch (error) {
            console.error(`Erreur lors de la notification d'action employé: ${error.message}`);
            return { success: false, error: error.message };
        }
    },
    
    
    notifyChangementBeneficiaire: async (info, status, phoneNumber) => {
        try {
            let messageContent;
            
            switch (status) {
                case "EN_ATTENTE":
                    messageContent = `Cher(e) ${info.prenom} ${info.nom},

Nous accusons réception de votre demande de changement de bénéficiaire pour le contrat n°${info.numeroContrat}.

Votre demande est actuellement en cours d'étude par notre service.
Référence de votre demande: ${info.reference}

Nous vous informerons dès qu'une décision aura été prise.

Cordialement,
L'équipe  Assurances`;
                    break;
                case "CONFIRME":
                    messageContent = `Cher(e) ${info.prenom} ${info.nom},

Félicitations ! Votre demande de changement de bénéficiaire pour le contrat n°${info.numeroContrat} a été approuvée.

Cette modification est désormais effective dans nos systèmes.
Référence de votre demande: ${info.reference}
Date d'approbation: ${new Date().toLocaleString('fr-FR')}

Nous vous remercions pour votre confiance.

Cordialement,
L'équipe  Assurances`;
                    break;
                case "REFUSE":
                    messageContent = `Cher(e) ${info.prenom} ${info.nom},

Nous sommes au regret de vous informer que votre demande de changement de bénéficiaire pour le contrat n°${info.numeroContrat} n'a pas pu être acceptée.
${info.raison ? `\nMotif: ${info.raison}` : ''}

Référence de votre demande: ${info.reference}
Date de la décision: ${new Date().toLocaleString('fr-FR')}

Pour toute question concernant cette décision, veuillez contacter votre conseiller.

Cordialement,
L'équipe  Assurances`;
                    break;
                case "ANNULE":
                    messageContent = `Cher(e) ${info.prenom} ${info.nom},

Nous vous informons que votre demande de changement de bénéficiaire pour le contrat n°${info.numeroContrat} a été annulée.

Référence de votre demande: ${info.reference}
Date d'annulation: ${new Date().toLocaleString('fr-FR')}

Si vous n'êtes pas à l'origine de cette annulation, veuillez contacter votre conseiller dans les plus brefs délais.

Cordialement,
L'équipe  Assurances`;
                    break;
                default:
                    messageContent = `Cher(e) ${info.prenom} ${info.nom},

Nous vous informons que le statut de votre demande de changement de bénéficiaire pour le contrat n°${info.numeroContrat} a été mis à jour en "${status}".

Référence de votre demande: ${info.reference}
Date de mise à jour: ${new Date().toLocaleString('fr-FR')}

Pour plus d'informations, veuillez vous connecter à votre espace client ou contacter votre conseiller.

Cordialement,
L'équipe  Assurances`;
            }
            
            const message = `

${messageContent}
            `.trim();
            
            return await twilioService.sendWhatsAppMessage(phoneNumber, message);
        } catch (error) {
            console.error(`Erreur lors de la notification de changement bénéficiaire: ${error.message}`);
            return { success: false, error: error.message };
        }
    }
};

module.exports = twilioService; 