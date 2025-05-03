const { Sequelize } = require('sequelize');

const sequelize = new Sequelize('bpm_node_db', 'root', '', {
    host: 'localhost',
    dialect: 'mysql',
    logging: false
});

module.exports = sequelize; 