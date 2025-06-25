package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    // add additional method signatures here
    ShoppingCart addCart(int userId, int productId);
    ShoppingCart removeCart(int userId);
    ShoppingCart updateCart(int userId, int productId, int quantity);
}
