package biz.craftline.server.feature.ordermanagement.api.dto;

import java.util.Date;

/**
 * Data Transfer Object for VirtualProductDetails.
 * Encapsulates virtual product fulfillment details for an order item.
 */
public class VirtualProductDetailsDTO {
    /** License key for virtual product */
    private String licenseKey;
    /** Download URL for virtual product */
    private String downloadUrl;
    /** Activation status for virtual product */
    private String activationStatus;
    /** Unique identifier for virtual product details */
    private Long id;
    /** Activation date for virtual product */
    private Date activationDate;

    public String getLicenseKey() { return licenseKey; }
    public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getActivationStatus() { return activationStatus; }
    public void setActivationStatus(String activationStatus) { this.activationStatus = activationStatus; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Date getActivationDate() { return activationDate; }
    public void setActivationDate(Date activationDate) { this.activationDate = activationDate; }
}