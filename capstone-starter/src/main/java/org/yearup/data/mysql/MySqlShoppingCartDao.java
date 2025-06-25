package org.yearup.data.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.util.List;

@Component
public class MySqlShoppingCartDao implements ShoppingCartDao
{
    private final JdbcTemplate jdbcTemplate;
    private final ProductDao productDao;

    public MySqlShoppingCartDao(JdbcTemplate jdbcTemplate, ProductDao productDao)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId)
    {
        String sql = "SELECT product_id, quantity FROM shopping_cart WHERE user_id = ?";
        List<ShoppingCartItem> items = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int productId = rs.getInt("product_id");
            int quantity = rs.getInt("quantity");

            Product product = productDao.getById(productId);
            ShoppingCartItem item = new ShoppingCartItem();
            item.setProduct(product);
            item.setQuantity(quantity);

            return item;
        }, userId);

        ShoppingCart cart = new ShoppingCart();
        for (ShoppingCartItem item : items)
        {
            cart.add(item);
        }

        return cart;
    }

    @Override
    public void addProduct(int userId, int productId)
    {
        String selectSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        List<Integer> quantities = jdbcTemplate.query(selectSql, (rs, rowNum) -> rs.getInt("quantity"), userId, productId);

        if (quantities.isEmpty())
        {
            String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)";
            jdbcTemplate.update(insertSql, userId, productId);
        }
        else
        {
            int currentQty = quantities.get(0);
            String updateSql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
            jdbcTemplate.update(updateSql, currentQty + 1, userId, productId);
        }
    }

    @Override
    public void updateQuantity(int userId, int productId, int quantity)
    {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        jdbcTemplate.update(sql, quantity, userId, productId);
    }

    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
