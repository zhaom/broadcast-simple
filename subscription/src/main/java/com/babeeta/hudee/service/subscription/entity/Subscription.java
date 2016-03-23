package com.babeeta.hudee.service.subscription.entity;

import java.util.Date;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

/**
 * subscription record entity
 * 
 */
@Entity(value = "subscription", noClassnameStored = true)
public class Subscription {
	@Id
	private String id;

	@Indexed
	private String key;
	@Indexed
	private String did;
	@Indexed
	private String owner;

	private String tName;

	private Date createdAt;

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
	 * @return the did
	 */
	public String getDid() {
		return did;
	}

	/**
	 * @param did
	 *            the did to set
	 */
	public void setDid(String did) {
		this.did = did;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the tName
	 */
	public String gettName() {
		return tName;
	}

	/**
	 * @param tName
	 *            the tName to set
	 */
	public void settName(String tName) {
		this.tName = tName;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}