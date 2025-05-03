const express = require('express');
const router = express.Router();
const Assurance = require('../models/Assurance');
const twilioService = require('../services/twilioService');

/**
 * @swagger
 * /api/assurances:
 *   get:
 *     summary: Récupérer toutes les assurances
 *     description: Retourne la liste des contrats d'assurance
 */
router.get('/', async (req, res) => {
    try {
        const assurances = await Assurance.findAll();
        res.json(assurances);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

/**
 * @swagger
 * /api/assurances/confirmer-changement:
 *   post:
 *     summary: Confirmer un changement de bénéficiaire
 *     description: Met à jour le statut d'une assurance suite à un changement
 */
router.post('/confirmer-changement', async (req, res) => {
    try {
        const assurance = await Assurance.findOne({
            where: { numeroContrat: req.body.numeroContrat }
        });

        if (!assurance) {
            return res.status(404).json({ message: 'Assurance non trouvée' });
        }

        await assurance.update({
            changementId: req.body.changementId,
            dateChangement: new Date(),
            status: 'CONFIRME'
        });

        res.status(200).json({ message: 'Changement confirmé' });
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});

/**
 * @swagger
 * /api/assurances/from-employe:
 *   post:
 *     summary: Créer ou mettre à jour une assurance
 *     description: Crée ou met à jour un contrat d'assurance pour un employé
 */
router.post('/from-employe', async (req, res) => {
    try {
        const { employe, utilisateur, numeroContrat, action } = req.body;

        if (!employe || !utilisateur) {
            return res.status(400).json({
                success: false,
                message: "Les données d'employé et d'utilisateur sont requises"
            });
        }

        let assurance = null;
        let isNew = false;

        if (numeroContrat) {
            assurance = await Assurance.findOne({
                where: { numeroContrat: numeroContrat }
            });
        }

        if (!assurance) {
            isNew = true;
            assurance = await Assurance.create({
                nomCompagnie: "MAMA",
                numeroContrat: numeroContrat,
                employeId: employe.id,
                status: 'ACTIF',
                dateCreation: new Date()
            });
        } else {
            await assurance.update({
                employeId: employe.id,
                dateModification: new Date()
            });
        }

        const notificationAction = isNew ? "CREATION_ASSURANCE" : "MISE_A_JOUR_ASSURANCE";
        const notificationMessage = isNew
            ? `Nouvelle assurance n°${numeroContrat} créée pour ${utilisateur.prenom} ${utilisateur.nom}`
            : `Assurance n°${numeroContrat} mise à jour pour ${utilisateur.prenom} ${utilisateur.nom}`;

        try {
            const notificationData = {
                message: notificationMessage,
                type: 'ASSURANCE_ACTION',
                employeId: employe.id,
                action: notificationAction,
                destinataire: utilisateur.telephone || null
            };

            await fetch('http://localhost:3001/api/notifications', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(notificationData),
            });

            if (utilisateur.telephone) {
                const whatsappMessage = `🏢 BPM Assurance 🏢
                            ${isNew ? 'Nouvelle assurance créée' : 'Assurance mise à jour'}
                            Contrat: ${numeroContrat}
                            Pour: ${utilisateur.prenom} ${utilisateur.nom}
                            Employé n°: ${employe.numeroEmploye}
                            Date: ${new Date().toLocaleString('fr-FR')}`;

                twilioService.sendWhatsAppMessage(utilisateur.telephone, whatsappMessage);
            }
        } catch (notifError) {
            console.error("Erreur lors de l'envoi de la notification:", notifError);
        }

        res.status(isNew ? 201 : 200).json({
            success: true,
            message: isNew ? "Assurance créée avec succès" : "Assurance mise à jour avec succès",
            assurance: assurance
        });
    } catch (error) {
        console.error("Erreur lors de la création/mise à jour de l'assurance:", error);
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

/**
 * @swagger
 * /api/assurances:
 *   post:
 *     summary: Créer une nouvelle assurance
 *     description: Ajoute un nouveau contrat d'assurance
 */
router.post('/', async (req, res) => {
    try {
        const assurance = await Assurance.create({
            nomCompagnie: req.body.nomCompagnie,
            numeroContrat: req.body.numeroContrat,
            employeId: req.body.employe?.id,
            status: 'ACTIF'
        });
        res.status(201).json(assurance);
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});

/**
 * @swagger
 * /api/assurances/contrat/{numeroContrat}:
 *   get:
 *     summary: Récupérer une assurance par numéro
 *     description: Trouve une assurance par son numéro de contrat
 */
router.get('/contrat/:numeroContrat', async (req, res) => {
    try {
        const assurance = await Assurance.findOne({
            where: { numeroContrat: req.params.numeroContrat }
        });

        if (!assurance) {
            return res.status(404).json({ message: 'Assurance non trouvée' });
        }

        res.json(assurance);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

/**
 * @swagger
 * /api/assurances/{id}:
 *   delete:
 *     summary: Supprimer une assurance
 *     description: Supprime un contrat d'assurance du système
 */
router.delete('/:id', async (req, res) => {
    try {
        const result = await Assurance.destroy({
            where: { id: req.params.id }
        });

        if (result === 0) {
            return res.status(404).json({ message: 'Assurance non trouvée' });
        }

        res.status(200).json({ message: 'Assurance supprimée' });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

/**
 * @swagger
 * /api/assurances/check/{numeroContrat}:
 *   get:
 *     summary: Vérifier l'existence d'une assurance
 *     description: Vérifie si une assurance existe par son numéro de contrat
 */
router.get('/check/:numeroContrat', async (req, res) => {
    try {
        const assurance = await Assurance.findOne({
            where: { numeroContrat: req.params.numeroContrat }
        });

        res.status(200).json({
            exists: !!assurance,
            message: assurance ? 'Assurance trouvée' : 'Assurance non trouvée'
        });
    } catch (error) {
        res.status(500).json({ 
            exists: false,
            message: error.message 
        });
    }
});

module.exports = router; 