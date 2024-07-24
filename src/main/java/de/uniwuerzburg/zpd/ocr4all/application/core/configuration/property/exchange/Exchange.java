/**
 * File:     Exchange.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.exchange
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.05.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.exchange;

import jakarta.validation.constraints.NotEmpty;

/**
 * Defines ocr4all exchange properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class Exchange {
	/**
	 * The folder.
	 */
	@NotEmpty(message = "the ocr4all exchange folder cannot be null nor empty")
	private String folder;

	/**
	 * The partition.
	 */
	private Partition partition = new Partition();

	/**
	 * Returns the folder.
	 *
	 * @return The folder.
	 * @since 1.8
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * Set the folder.
	 *
	 * @param folder The folder to set.
	 * @since 1.8
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * Returns the partition.
	 *
	 * @return The partition.
	 * @since 17
	 */
	public Partition getPartition() {
		return partition;
	}

	/**
	 * Set the partition.
	 *
	 * @param partition The partition to set.
	 * @since 17
	 */
	public void setPartition(Partition partition) {
		this.partition = partition;
	}

}
