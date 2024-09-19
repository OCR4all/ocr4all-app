/**
 * File:     SPIUtils.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.util
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     19.09.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.util;

import java.util.ArrayList;
import java.util.List;

import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.Argument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.BooleanArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.DecimalArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ImageArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.IntegerArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.SelectArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.StringArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.WeightArgument;

/**
 * Defines ocr4all utilities.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class SPIUtils {
	/**
	 * Returns the service provider model with their arguments.
	 * 
	 * @param serviceProviderArgument The service provider arguments.
	 * @return The service provider model with their arguments.
	 * @since 17
	 */
	public static ModelArgument getModelArgument(ServiceProvider serviceProviderArgument) {
		List<Argument> arguments = new ArrayList<>();

		/*
		 * The boolean arguments.
		 */
		if (serviceProviderArgument.getBooleans() != null)
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.BooleanArgument bool : serviceProviderArgument
					.getBooleans())
				if (bool != null)
					arguments.add(new BooleanArgument(bool.getArgument(), bool.getValue()));

		/*
		 * The decimal arguments.
		 */
		if (serviceProviderArgument.getDecimals() != null)
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.DecimalArgument decimal : serviceProviderArgument
					.getDecimals())
				if (decimal != null)
					arguments.add(new DecimalArgument(decimal.getArgument(), decimal.getValue()));

		/*
		 * The integer arguments.
		 */
		if (serviceProviderArgument.getIntegers() != null)
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.IntegerArgument integer : serviceProviderArgument
					.getIntegers())
				if (integer != null)
					arguments.add(new IntegerArgument(integer.getArgument(), integer.getValue()));

		/*
		 * The string arguments.
		 */
		if (serviceProviderArgument.getStrings() != null)
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.StringArgument string : serviceProviderArgument
					.getStrings())
				if (string != null)
					arguments.add(new StringArgument(string.getArgument(), string.getValue()));

		/*
		 * The image arguments.
		 */
		if (serviceProviderArgument.getImages() != null)
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ImageArgument image : serviceProviderArgument
					.getImages())
				if (image != null)
					arguments.add(new ImageArgument(image.getArgument(), image.getValues()));

		/*
		 * The weight arguments.
		 */
		if (serviceProviderArgument.getWeights() != null)
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.WeightArgument recognitionModel : serviceProviderArgument
					.getWeights())
				if (recognitionModel != null) {
					List<WeightArgument.Assemble> assembles = new ArrayList<>();
					if (recognitionModel.getAssembles() != null)
						for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.WeightArgument.Assemble assemble : recognitionModel
								.getAssembles())
							if (assemble != null)
								assembles.add(new WeightArgument.Assemble(assemble.getId(), assemble.getModels()));

					arguments.add(new WeightArgument(recognitionModel.getArgument(), assembles));
				}

		/*
		 * The select arguments.
		 */
		if (serviceProviderArgument.getSelects() != null)
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.SelectArgument select : serviceProviderArgument
					.getSelects())
				if (select != null)
					arguments.add(new SelectArgument(select.getArgument(), select.getValues()));

		/*
		 * The model argument
		 */
		return new ModelArgument(arguments);
	}

}
