const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Assurance = sequelize.define('Assurance', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    nomCompagnie: {
        type: DataTypes.STRING,
        allowNull: false
    },
    numeroContrat: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true
    },
    employeId: {
        type: DataTypes.INTEGER,
        allowNull: true
    },
    changementId: {
        type: DataTypes.INTEGER,
        allowNull: true
    },
    dateChangement: {
        type: DataTypes.DATE,
        allowNull: true
    },
    dateCreation: {
        type: DataTypes.DATE,
        allowNull: true,
        defaultValue: DataTypes.NOW
    },
    dateModification: {
        type: DataTypes.DATE,
        allowNull: true
    },
    status: {
        type: DataTypes.STRING,
        allowNull: true,
        defaultValue: 'A_CONFIRMER'
    },
    donnees: {
        type: DataTypes.TEXT,
        allowNull: true,
        get() {
            const rawValue = this.getDataValue('donnees');
            return rawValue ? JSON.parse(rawValue) : null;
        },
        set(value) {
            this.setDataValue('donnees', value ? JSON.stringify(value) : null);
        }
    }
}, {
    tableName: 'assurances',
    timestamps: true
});

module.exports = Assurance; 