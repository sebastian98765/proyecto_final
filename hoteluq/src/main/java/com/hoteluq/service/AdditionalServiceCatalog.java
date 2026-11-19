package com.hoteluq.service;

import com.hoteluq.model.AdditionalService;
import java.util.*;

/** Catálogo de servicios adicionales administrable en memoria. */
public class AdditionalServiceCatalog {
    private final Map<String, AdditionalService> catalog = new LinkedHashMap<>();

    public AdditionalServiceCatalog() {
        save("Desayuno", 35_000, true);
        save("Parqueadero", 20_000, true);
        save("Spa", 90_000, true);
        save("Transporte", 60_000, true);
    }

    public synchronized AdditionalService save(String name, double price, boolean available) {
        if (name == null || name.isBlank() || !Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("Nombre o precio de servicio inválido");
        }
        AdditionalService service = new AdditionalService(name.trim(), price, available);
        catalog.put(name.trim().toLowerCase(Locale.ROOT), service);
        return service;
    }

    public synchronized List<AdditionalService> all() {
        return List.copyOf(catalog.values());
    }

    public synchronized List<AdditionalService> select(List<String> names) {
        Objects.requireNonNull(names, "Los servicios son obligatorios");
        List<AdditionalService> selected = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String name : names) {
            if (name == null || !seen.add(name.trim().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Servicio duplicado o inválido");
            }
            AdditionalService service = catalog.get(name.trim().toLowerCase(Locale.ROOT));
            if (service == null || !service.available()) {
                throw new IllegalArgumentException("Servicio no disponible: " + name);
            }
            selected.add(service);
        }
        return List.copyOf(selected);
    }
}
