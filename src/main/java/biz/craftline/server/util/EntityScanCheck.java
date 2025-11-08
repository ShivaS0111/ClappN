package biz.craftline.server.util;

import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import jakarta.persistence.metamodel.EntityType;

/*
@Component
public class EntityScanCheck implements CommandLineRunner {

    private final EntityManager em;

    public EntityScanCheck(EntityManager em) {
        this.em = em;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Entities Registered ===");
        for (EntityType<?> entity : em.getMetamodel().getEntities()) {
            System.out.println(entity.getName() + " -> " + entity.getJavaType());
        }
    }
}*/
