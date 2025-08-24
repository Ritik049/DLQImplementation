package com.ecommerce.order.service;


import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {


   private final CartItemRepository cartItemRepository;

//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    ProductRepository productRepository;

//    private final ProductServiceClient productServiceClient;

//    private final UserServiceClient userServiceClient;
    int attempt = 0;


   // @CircuitBreaker(name = "productService" , fallbackMethod = "addToCartFallBack")
    @Retry(name="retryBreaker", fallbackMethod = "addToCartFallBack")
    public boolean  addToCart(String userId, CartItemRequest request)
    {
        System.out.println("ATTEMPT COUNT: "+ ++attempt);
        //checking purpose
//        ProductResponse productResponse = productServiceClient.getProductDetails(request.getProductId()+"");
//
//        if(productResponse==null) return false;
//
//
//
//        if(productResponse.getStockQuantity() < request.getQuantity())
//        {
//            return false;
//        }
//
//        UserResponse  userResponse = userServiceClient.getUserDetails(userId);
//
//        if(userResponse==null)return false;
//
//        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
//
//        if(userOpt.isEmpty()) return false;
//
//        User user = userOpt.get();

        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId,request.getProductId()+"");
        if(existingCartItem!=null) {
             // Updated the quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
//            existingCartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(existingCartItem.getQuantity())));
             existingCartItem.setPrice(BigDecimal.valueOf(1000.00));
            cartItemRepository.save(existingCartItem);
        }else {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId()+"");
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(BigDecimal.valueOf(400));
//           cartItem.setPrice(productResponse.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
          //  cartItem.setPrice(BigDecimal.valueOf(1000.00));
            cartItemRepository.save(cartItem);
        }

        return true;
    }

    public boolean addToCartFallBack(String userId, CartItemRequest request,Exception exception)
    {
        // System.out.println("FALLBACK CALLED");
        exception.printStackTrace();
        return false;
    }

    public boolean deleteItemFromCart(String userId, Long productId){
      //  Optional<Product> productOpt  = productRepository.findById(productId);
     //   Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));

//        if(productOpt.isPresent() && userOpt.isPresent())
//        {
//            cartItemRepository.deleteByUserAndProduct(userOpt.get(),productOpt.get());
//            return true;
//        }
          CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId,productId+"");

          if(cartItem!=null)
          {
              cartItemRepository.delete(cartItem);
              return true;
          }

        return false;
    }

    public List<CartItem> getCart(String userId)
    {
        //return userRepository.findById(Long.valueOf(userId)).map(cartItemRepository::findByUser).orElseGet(List::of);
         return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(String userId) {
        //userRepository.findById(Long.valueOf(userId)).ifPresent(cartItemRepository::deleteByUser);
        cartItemRepository.deleteByUserId(userId);
    }
}
