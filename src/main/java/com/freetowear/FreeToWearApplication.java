package com.freetowear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.freetowear.entity.Category;
import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;
import com.freetowear.entity.ProductVariationImage;
import com.freetowear.enums.Size;
import com.freetowear.repository.CategoryRepository;
import com.freetowear.repository.ProductRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@SpringBootApplication
public class FreeToWearApplication {

	private static final Pattern IMAGE = Pattern.compile("src=\"([^\"]+)\".*?alt=\"([^\"]+)\"");
	private static final Pattern SHIRT_COLOR = Pattern.compile("<path[^>]*fill=\"(#[0-9a-fA-F]{6})\"");
	private static final Map<String, String> COLOR_NAMES = Map.of(
			"#18191d", "Preto", "#faf7ed", "Off-white", "#f3b2bd", "Rosa",
			"#273e36", "Verde", "#24374d", "Azul-marinho", "#273348", "Azul-marinho",
			"#abc9ef", "Azul-claro"
	);

	@Bean
	CommandLineRunner catalogSeeder(CategoryRepository categories, ProductRepository products, JdbcTemplate jdbc) {
		return args -> {
			jdbc.execute("ALTER TABLE product_variation_image MODIFY image_public_id LONGTEXT NOT NULL");
			seedCatalog(categories, products);
		};
	}

	@Transactional
	void seedCatalog(CategoryRepository categories, ProductRepository products) throws Exception {
		Category category = categories.findByNameIgnoreCase("Camisetas").orElseGet(() -> {
			Category created = new Category();
			created.setName("Camisetas");
			return categories.save(created);
		});

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new ClassPathResource("data/svgs.txt").getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			int index = 0;
			while ((line = reader.readLine()) != null) {
				Matcher match = IMAGE.matcher(line);
				if (!match.find()) continue;
				String name = match.group(2).replaceFirst("^Exemplo ilustrado de camiseta: ", "").trim();
				String color = extractShirtColor(match.group(1));
				var existing = products.findByName(name);
				if (existing.isPresent()) {
					existing.get().getVariations().forEach(variation -> variation.setColor(color));
					products.save(existing.get());
					continue;
				}

				String fabric = List.of("Algodão", "Poliéster", "Linho").get(index % 3);
				Product product = new Product();
				product.setName(name);
				product.setDescription("Camiseta de " + fabric + " com estampa exclusiva.");
				product.setPrice(BigDecimal.valueOf(59 + (index % 4) * 10L));
				product.setCategory(category);
				product.setVariations(new ArrayList<>());

				for (Size size : Size.values()) {
					ProductVariation variation = new ProductVariation();
					variation.setProduct(product);
					variation.setColor(color);
					variation.setSize(size);
					variation.setStock(10);
					ProductVariationImage image = new ProductVariationImage();
					image.setProductVariation(variation);
					image.setImagePublicId(match.group(1));
					variation.getImages().add(image);
					product.getVariations().add(variation);
				}
				products.save(product);
				index++;
			}
		}
	}

	private String extractShirtColor(String imageDataUri) {
		String decoded = URLDecoder.decode(imageDataUri, StandardCharsets.UTF_8);
		Matcher matcher = SHIRT_COLOR.matcher(decoded);
		if (!matcher.find()) return "Variada";
		String hex = matcher.group(1).toLowerCase();
		return COLOR_NAMES.getOrDefault(hex, hex.toUpperCase());
	}

	public static void main(String[] args) {
		SpringApplication.run(FreeToWearApplication.class, args);
	}
}