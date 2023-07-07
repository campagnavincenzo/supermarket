package com.app.SuperMarketSystem.service;

import com.app.SuperMarketSystem.dto.ApiResponse;
import com.app.SuperMarketSystem.dto.ProductDTO;
import com.app.SuperMarketSystem.model.Order;
import com.app.SuperMarketSystem.model.Product;
import com.app.SuperMarketSystem.model.User;
import com.app.SuperMarketSystem.repository.OrderRepository;
import com.app.SuperMarketSystem.repository.ProductRepository;
import com.app.SuperMarketSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, UserRepository userRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public ApiResponse findAllProducts() {
        ApiResponse apiResponse = new ApiResponse();
        try {
            List<Product> productList = productRepository.findAll();
            if (productList.isEmpty()) {
                apiResponse.setMessage("No products found within the database");
                apiResponse.setStatus(HttpStatus.NOT_FOUND.value());
                apiResponse.setData(null);
            } else {
                apiResponse.setMessage("Successfully fetched products from the database");
                apiResponse.setData(productList);
                apiResponse.setStatus(HttpStatus.OK.value());
            }
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setData(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return apiResponse;
        }
    }

    public ApiResponse addProduct(Product product) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            productRepository.save(product);
            apiResponse.setMessage("Successfully added product within the database");
            apiResponse.setData(product);
            apiResponse.setStatus(HttpStatus.OK.value());
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setData(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return apiResponse;
        }
    }

    public ApiResponse deleteProduct(String productId) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            Product product = productRepository.getById(productId);
            if (null != product) {
                productRepository.delete(product);
                apiResponse.setStatus(HttpStatus.OK.value());
                apiResponse.setMessage("Successfully deleted product from the database");
            } else {
                apiResponse.setStatus(HttpStatus.NOT_FOUND.value());
                apiResponse.setMessage("No such product found against this ID");
            }
            apiResponse.setData(null);
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setData(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return apiResponse;
        }
    }

    public ApiResponse updateProduct(Product updatedProduct) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            Product existingProduct = productRepository.getById(updatedProduct.getId());
            if (null != existingProduct) {
                productRepository.save(updatedProduct);
                apiResponse.setMessage("Successfully updated product within the database");
                apiResponse.setData(updatedProduct);
                apiResponse.setStatus(HttpStatus.OK.value());
            } else {
                apiResponse.setStatus(HttpStatus.NOT_FOUND.value());
                apiResponse.setMessage("No such product found against this ID");
                apiResponse.setData(null);
            }
            return apiResponse;

        } catch (Exception e) {
            apiResponse.setData(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return apiResponse;
        }
    }

    public ApiResponse getProductById(String productId) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            Product product = productRepository.getById(productId);
            if (null != product) {
                apiResponse.setStatus(HttpStatus.OK.value());
                apiResponse.setMessage("Successful");
                apiResponse.setData(product);
            } else {
                apiResponse.setData(null);
                apiResponse.setStatus(HttpStatus.NOT_FOUND.value());
                apiResponse.setMessage("No such product found within the database");
            }
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setData(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return apiResponse;
        }
    }

    public ApiResponse purchaseProducts(Integer userId, List<ProductDTO> productsList) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            User user = userRepository.getById(userId);
            if (null != user) {
                Order order = new Order();
                order.setTotalPrice(0.0);
                for (ProductDTO product : productsList
                ) {
                    Product productToBuy = productRepository.getById(product.getProductId());
                    if (null != productToBuy) {
                        order.setTotalPrice(productToBuy.getPrice() * product.getQuantityToPurchase() + order.getTotalPrice());
                        order.getProducts().add(productToBuy);
                    } else {
                        apiResponse.setData(null);
                        apiResponse.setStatus(HttpStatus.NOT_FOUND.value());
                        apiResponse.setMessage("No such product found against " + product.getProductId() + " within the database");
                    }
                }
                order.setDeliveryStatus("Pending");
                order.setOrderTime(LocalDateTime.now());
                orderRepository.save(order);
                user.getOrders().add(order);

                apiResponse.setStatus(HttpStatus.OK.value());
                apiResponse.setMessage("Successfully placed the orders");
                apiResponse.setData(order);
                userRepository.save(user);
            } else {
                apiResponse.setData(null);
                apiResponse.setStatus(HttpStatus.NOT_FOUND.value());
                apiResponse.setMessage("No such user found against this ID within the database");
            }
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setData(null);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return apiResponse;
        }
    }
}