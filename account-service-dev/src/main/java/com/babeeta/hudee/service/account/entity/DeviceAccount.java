package com.babeeta.hudee.service.account.entity;

import java.util.Date;
import java.util.Map;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

/**
 * Device account entity
 */
@Entity(value = "account", noClassnameStored = true)
public class DeviceAccount {
	@Id
	private String id;
	private String key;

	@Indexed
	private String identifier;

	private boolean isPushMode;

	private Date activateAt;

	@Embedded(concreteClass = java.util.HashMap.class)
	private Map<String, Object> extra;

	/**
	 * Constructor
	 */
	public DeviceAccount() {
		super();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the activateAt
	 */
	public Date getActivateAt() {
		return activateAt;
	}

	/**
	 * @param activateAt
	 *            the activateAt to set
	 */
	public void setActivateAt(Date activateAt) {
		this.activateAt = activateAt;
	}

	/**
	 * @return the extra
	 */
	public Map<String, Object> getExtra() {
		return extra;
	}

	/**
	 * @param extra
	 *            the extra to set
	 */
	public void setExtra(Map<String, Object> extra) {
		this.extra = extra;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
     * @return the isPushMode
     */
    public boolean isPushMode() {
	    return isPushMode;
    }

	/**
     * @param isPushMode the isPushMode to set
     */
    public void setPushMode(boolean isPushMode) {
	    this.isPushMode = isPushMode;
    }
}
