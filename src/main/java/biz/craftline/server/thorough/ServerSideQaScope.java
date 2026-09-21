package biz.craftline.server.thorough;

/**
 * Canonical server-side QA / authorization scope for ClappN thorough suites.
 * <p>
 * Finding categories:
 * <ul>
 *   <li>{@link FindingCategory#TENANT} (A) — Is the user prevented from accessing another
 *       business/store's tenant-owned data?</li>
 *   <li>{@link FindingCategory#PERMISSION} (B) — Does the user have the required permission
 *       for the operation?</li>
 *   <li>{@link FindingCategory#SHARED_CATALOG} (C) — Is the user allowed to access/use a
 *       global or business-type-based template/catalog item?</li>
 * </ul>
 * Report sections (1–26) match the final SERVER-SIDE QA &amp; AUTHORIZATION REPORT outline.
 */
public final class ServerSideQaScope {

    private ServerSideQaScope() {}

    /** The 15 major test areas from the final QA scope. */
    public static final String[] AREAS = {
            "1. Authentication",
            "2. Authorization — Roles & Permissions",
            "3. Tenant Isolation",
            "4. Business-Level vs Store-Level Access",
            "5. CRUD Security",
            "6. Products",
            "7. Service Templates / Product Templates",
            "8. Global Catalogs",
            "9. Admin-Only APIs",
            "10. Indirect / Chained Authorization",
            "11. IDOR / Object-Level Authorization",
            "12. Validation & Error Handling",
            "13. Data Integrity",
            "14. Concurrency / State",
            "15. Test Data"
    };

    /** The 26 report sections in the final deliverable. */
    public static final String[] REPORT_SECTIONS = {
            "1. Executive Summary",
            "2. Test Environment",
            "3. Test Dataset",
            "4. User / Role / Permission Matrix",
            "5. Membership / Employee Scope Matrix",
            "6. Endpoint Coverage",
            "7. Authentication Results",
            "8. Authorization Results",
            "9. Tenant Isolation Results",
            "10. Business-Level Access Results",
            "11. Store-Level Access Results",
            "12. CRUD Results",
            "13. Product Results",
            "14. Service Results",
            "15. Template / Catalog Results",
            "16. Admin-Only API Results",
            "17. IDOR Results",
            "18. Indirect Authorization Results",
            "19. Validation Results",
            "20. Data Integrity Results",
            "21. Bugs / Findings",
            "22. Security Findings",
            "23. Regression Findings",
            "24. Coverage Statistics",
            "25. Recommendations",
            "26. Final PASS / FAIL Assessment"
    };

    public enum FindingCategory {
        /** A — tenant / business / store isolation */
        TENANT("A", "Tenant security"),
        /** B — role/permission gate */
        PERMISSION("B", "Permission security"),
        /** C — global / business-type catalog & templates */
        SHARED_CATALOG("C", "Shared catalog authorization");

        private final String code;
        private final String label;

        FindingCategory(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String code() {
            return code;
        }

        public String label() {
            return label;
        }
    }
}
