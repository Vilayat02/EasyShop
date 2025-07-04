package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    private ShoppingCartDao shoppingCartDao;

    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        String query = "SELECT shopping_cart.quantity, products.* " +
                "FROM shopping_cart " +
                "JOIN products ON products.product_id = shopping_cart.product_id " +
                "WHERE shopping_cart.user_id = ?;";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet result = statement.executeQuery();
            ShoppingCart shoppingCart = new ShoppingCart();

            while (result.next()) {
                int productID = result.getInt("product_id");
                String name = result.getString("name");
                BigDecimal price = result.getBigDecimal("price");
                int category = result.getInt("category_id");
                String description = result.getString("description");
                String color = result.getString("color");
                int stock = result.getInt("stock");
                boolean isFeatured = result.getBoolean("featured");
                String imageUrl = result.getString("image_url");
                int quantity = result.getInt("quantity");
                Product product = new Product(productID, name, price, category, description, color, stock, isFeatured, imageUrl);
                ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                shoppingCartItem.setProduct(product);
                shoppingCartItem.setQuantity(quantity);
                shoppingCart.add(shoppingCartItem);
            }
            return shoppingCart;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ShoppingCart addCart(int userId, int productId) {
        ShoppingCart shoppingCart = new ShoppingCart();
        String query1 = " SELECT quantity " +
                " FROM shopping_cart " +
                " WHERE user_id = ? " +
                " AND product_id = ? ";
        try (Connection connection = getConnection()) {
            PreparedStatement searchStatement = connection.prepareStatement(query1);
            searchStatement.setInt(1, userId);
            searchStatement.setInt(2, productId);
            ResultSet result = searchStatement.executeQuery();
            if (result.next() == false) {
                String query2 = " INSERT INTO shopping_cart(user_id, product_id, quantity) " +
                        " VALUES (?, ?, ?); ";
                PreparedStatement addStatement = connection.prepareStatement(query2);
                addStatement.setInt(1, userId);
                addStatement.setInt(2, productId);
                addStatement.setInt(3, 1);
                addStatement.executeUpdate();
                System.out.println("Item with Product ID: " + productId + " successfully added to cart.");
            } else {
                int quantity = result.getInt("quantity");
                String query3 = " UPDATE shopping_cart " +
                        " SET quantity = ? " +
                        " WHERE user_id = ? " +
                        " AND product_id = ?; ";
                PreparedStatement quantityStatement = connection.prepareStatement(query3);
                quantityStatement.setInt(1, quantity + 1);
                quantityStatement.setInt(2, userId);
                quantityStatement.setInt(3, productId);
                quantityStatement.executeUpdate();
                System.out.println("Item with Product ID: " + productId + " successfully added to cart and quantity updated.");
            }
            String query = " SELECT shopping_cart.quantity, products.*, users.username " +
                    " FROM shopping_cart " +
                    " JOIN products " +
                    " ON products.product_id = shopping_cart.product_id " +
                    " JOIN users " +
                    " ON shopping_cart.user_id = users.user_id" +
                    " WHERE shopping_cart.user_id = ?; ";

            try (Connection connection2 = getConnection()) {
                PreparedStatement statement2 = connection2.prepareStatement(query);
                statement2.setInt(1, userId);
                ResultSet result2 = statement2.executeQuery();

                while (result2.next()) {
                    int productID = result2.getInt("product_id");
                    String name = result2.getString("name");
                    BigDecimal price = result2.getBigDecimal("price");
                    int category = result2.getInt("category_id");
                    String description = result2.getString("description");
                    String color = result2.getString("color");
                    int stock = result2.getInt("stock");
                    boolean isFeatured = result2.getBoolean("featured");
                    String imageUrl = result2.getString("image_url");
                    int quantity = result2.getInt("quantity");
                    Product product = new Product(productID, name, price, category, description, color, stock, isFeatured, imageUrl);
                    ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                    shoppingCartItem.setProduct(product);
                    shoppingCartItem.setQuantity(quantity);
                    shoppingCart.add(shoppingCartItem);
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return shoppingCart;
    }

    @Override
    public ShoppingCart removeCart(int userId) {
        ShoppingCart shoppingCart = new ShoppingCart();
        String query = " DELETE FROM shopping_cart " +
                " WHERE user_id = ? ";
        try (Connection connection = getConnection()) {
            PreparedStatement searchStatement = connection.prepareStatement(query);
            searchStatement.setInt(1, userId);
            int rows = searchStatement.executeUpdate();


            if (rows > 0) {
                System.out.println("Cart with User ID: " + userId + " deleted successfully.");
            } else {
                System.out.println("No cart found with User ID: " + userId);
            }
        } catch(SQLException e)

        {
            e.printStackTrace();
        }
        System.out.println("Returning cart... ");
        return shoppingCart;
    }

    @Override
    public ShoppingCart updateCart(int userId, int productId, int quantity) {
        String sql = """
            UPDATE shopping_cart
            SET quantity = ?
            WHERE user_id = ? AND product_id = ?;
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);
            statement.executeUpdate();

            return getByUserId(userId);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

}
