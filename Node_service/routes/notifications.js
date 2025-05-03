const express = require('express');
const router = express.Router();
const Notification = require('../models/Notification');
const twilioService = require('../services/twilioService');

/**
 * @swagger
 * /api/notifications:
 *   get:
 *     summary: Récupérer toutes les notifications
 *     description: Retourne la liste complète des notifications
 */
router.get('/', async (req, res) => {
    try {
        const notifications = await Notification.findAll();
        res.json(notifications);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

/**
 * @swagger
 * /api/notifications:
 *   post:
 *     summary: Créer une notification avec envoi WhatsApp
 *     description: Crée une notification et l'envoie par WhatsApp si un numéro est fourni
 */
router.post('/', async (req, res) => {
    try {
        const notification = await Notification.create({
            message: req.body.message,
            type: req.body.type,
            employeId: req.body.employeId,
            destinataire: req.body.telephone || null
        });
        
        let whatsappResult = { success: false, reason: "Pas de numéro de téléphone" };
        if (req.body.telephone) {
            whatsappResult = await twilioService.sendWhatsAppMessage(
                req.body.telephone, 
                req.body.message
            );
            
            if (whatsappResult.success) {
                await notification.update({ 
                    status: 'SENT_WHATSAPP',
                    whatsappSid: whatsappResult.sid
                });
            } else {
                await notification.update({ 
                    status: 'WHATSAPP_FAILED',
                    errorMessage: whatsappResult.error
                });
            }
        }
        
        res.status(201).json({
            notification: notification,
            whatsapp: whatsappResult
        });
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});

/**
 * @swagger
 * /api/notifications/whatsapp:
 *   post:
 *     summary: Envoyer un message WhatsApp
 *     description: Envoie un message WhatsApp et enregistre une notification
 */
router.post('/whatsapp', async (req, res) => {
    try {
        const { telephone, message, type, employeId } = req.body;
        
        if (!telephone || !message) {
            return res.status(400).json({ 
                success: false,
                message: "Le numéro de téléphone et le message sont requis" 
            });
        }
        
        const result = await twilioService.sendWhatsAppMessage(telephone, message);
        
        if (result.success) {
            const notification = await Notification.create({
                message: message,
                type: type || 'WHATSAPP',
                employeId: employeId || null,
                destinataire: telephone,
                status: 'SENT_WHATSAPP',
                whatsappSid: result.sid,
                dateCreation: new Date(),
                dateEnvoi: new Date()
            });
            
            res.status(200).json({ 
                success: true, 
                notification: notification,
                sid: result.sid, 
                message: "Message WhatsApp envoyé et notification enregistrée" 
            });
        } else {
            const notification = await Notification.create({
                message: message,
                type: type || 'WHATSAPP',
                employeId: employeId || null,
                destinataire: telephone,
                status: 'WHATSAPP_FAILED',
                errorMessage: result.error,
                dateCreation: new Date()
            });
            
            res.status(500).json({ 
                success: false, 
                notification: notification,
                message: "Échec de l'envoi du message WhatsApp", 
                error: result.error 
            });
        }
    } catch (error) {
        res.status(500).json({ 
            success: false, 
            message: error.message 
        });
    }
});

/**
 * @swagger
 * /api/notifications/employe-action:
 *   post:
 *     summary: Notifier une action employé
 *     description: Enregistre et envoie une notification sur une action employé
 */
router.post('/employe-action', async (req, res) => {
    try {
        const { numeroEmploye, nom, prenom, action, telephone } = req.body;
        
        if (!numeroEmploye || !nom || !prenom || !action) {
            return res.status(400).json({ 
                message: "Les informations de l'employé et l'action sont requises" 
            });
        }
        
        const message = `Action: ${action}, Employé: ${prenom} ${nom} (${numeroEmploye})`;
        
        const notification = await Notification.create({
            message: message,
            type: 'EMPLOYE_ACTION',
            employeId: req.body.employeId || 0,
            destinataire: telephone || null
        });
        
        if (telephone) {
            const result = await twilioService.notifyEmployeAction(
                { numeroEmploye, nom, prenom }, 
                action, 
                telephone
            );
            
            if (result.success) {
                await notification.update({ 
                    status: 'SENT_WHATSAPP',
                    whatsappSid: result.sid
                });
                
                res.status(200).json({ 
                    success: true, 
                    notification: notification,
                    whatsapp: { success: true, sid: result.sid },
                    message: "Notification enregistrée et WhatsApp envoyé" 
                });
            } else {
                await notification.update({ 
                    status: 'WHATSAPP_FAILED',
                    errorMessage: result.error
                });
                
                res.status(200).json({ 
                    success: true, 
                    notification: notification,
                    whatsapp: { success: false, error: result.error },
                    message: "Notification enregistrée mais échec du WhatsApp" 
                });
            }
        } else {
            res.status(200).json({ 
                success: true, 
                notification: notification,
                message: "Notification enregistrée sans envoi WhatsApp" 
            });
        }
    } catch (error) {
        res.status(500).json({ 
            success: false, 
            message: error.message 
        });
    }
});

/**
 * @swagger
 * /api/notifications/{id}:
 *   get:
 *     summary: Récupérer une notification par ID
 *     description: Retourne les détails d'une notification spécifique
 */
router.get('/:id', async (req, res) => {
    try {
        const notification = await Notification.findByPk(req.params.id);
        
        if (!notification) {
            return res.status(404).json({ message: "Notification non trouvée" });
        }
        
        res.json(notification);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

module.exports = router; 