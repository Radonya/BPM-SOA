const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Notification = sequelize.define('Notification', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    message: {
        type: DataTypes.STRING,
        allowNull: false
    },
    type: {
        type: DataTypes.STRING,
        allowNull: false
    },
    employeId: {
        type: DataTypes.INTEGER,
        allowNull: false
    },
    destinataire: {
        type: DataTypes.STRING,
        allowNull: true
    },
    status: {
        type: DataTypes.STRING,
        allowNull: true,
        defaultValue: 'CREATED'
    },
    whatsappSid: {
        type: DataTypes.STRING,
        allowNull: true
    },
    errorMessage: {
        type: DataTypes.STRING,
        allowNull: true
    },
    dateCreation: {
        type: DataTypes.DATE,
        defaultValue: DataTypes.NOW
    }
}, {
    tableName: 'notifications',
    timestamps: true
});

module.exports = Notification;