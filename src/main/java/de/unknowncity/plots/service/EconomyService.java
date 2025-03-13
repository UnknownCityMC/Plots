package de.unknowncity.plots.service;

import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.configurration.EconomySettings;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.UUID;

public class EconomyService implements Service<PlotsPlugin> {
    private Currency currency;

    public EconomyService(EconomySettings economySettings) {
        currency = CoinsEngineAPI.getCurrency(economySettings.currency());
    }

    public void withdraw(UUID uuid, double amount) {
        CoinsEngineAPI.removeBalance(uuid, currency, amount);
    }

    public void deposit(UUID uuid, double amount) {
        CoinsEngineAPI.addBalance(uuid, currency, amount);
    }

    public boolean hasEnoughFunds(UUID uuid, double amount) {
        return CoinsEngineAPI.getBalance(uuid, currency) >= amount;
    }

    @Override
    public void shutdown() {

    }
}
