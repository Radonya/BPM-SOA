const express = require('express');
const cors = require('cors');
const sequelize = require('./config/database');
const fs = require('fs');
const path = require('path');
global.fetch = require('node-fetch'); // Rendre fetch disponible globalement

const app = express();
const port = 3001;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/notifications', require('./routes/notifications'));
app.use('/api/assurances', require('./routes/assurances'));

// Exposer la documentation OpenAPI
app.get('/v3/api-docs', (req, res) => {
    const openApiSpec = JSON.parse(fs.readFileSync(path.join(__dirname, 'openapi.json'), 'utf8'));
    res.json(openApiSpec);
});

// Synchroniser la base de données et démarrer le serveur
sequelize.sync()
    .then(() => {
        console.log('Base de données synchronisée');
        app.listen(port, () => {
            console.log(`Serveur BPM démarré sur le port ${port}`);
        });
    })
    .catch(err => {
        console.error('Erreur de synchronisation de la base de données:', err);
    }); 