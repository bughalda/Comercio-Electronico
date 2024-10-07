/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wspedido;

import fachadas.CustomerFacade;
import fachadas.CustomerOrderFacade;
import fachadas.OrderedProductFacade;
import fachadas.ProductFacade;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
/**
 *
 * @author RGAMBOAH
 */
@WebService(serviceName = "WSPedido")
public class WSPedido 
{

    @EJB
    private OrderedProductFacade orderedProductFacade;

    @EJB
    private CustomerOrderFacade customerOrderFacade;

    @EJB
    private ProductFacade productFacade;

    @EJB
    private CustomerFacade customerFacade;
    
    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        return "Hello " + txt + " !";
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "altaPedido")
    public int altaPedido(@WebParam(name = "id_clte") int id_clte, @WebParam(name = "lista_it") List<ClsItem> lista_it) 
    {
     
        
        List<entidades.Product>        lista_prods_en_pedido = new ArrayList<>();
        List<entidades.OrderedProduct> lista_orderedProducts = new ArrayList<>();
        
        entidades.OrderedProduct ordered_product;
        entidades.Product prod;
        
        float fltMontoPedido = (float)0.0;
              
        // se obtienen los productos involucrados
            for(ClsItem it: lista_it)
            {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                              "Product_id:" + it.getId_prod()+ ", cantidad:" + it.getCantidad());
                               prod = productFacade.find(new Integer(it.getId_prod()));
                              if(prod != null)
                              {
                                ordered_product = new entidades.OrderedProduct();
                                // falta la llave, dada por el producto y el customer order
                                ordered_product.setProduct(prod);
                                ordered_product.setQuantity((short)it.cantidad);
                                
                                lista_orderedProducts.add(ordered_product);
                                lista_prods_en_pedido.add(prod);
                                Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                                "Product_id:" + prod.getId()+ ", " + 
                                              prod.getName() + ", " + 
                                              prod.getDescription() + ", " + 
                                              prod.getCategoryId().getName() + ", " +
                                              prod.getPrice());
                                fltMontoPedido += prod.getPrice().doubleValue()*it.getCantidad();
                                // NOTA: Esta versión considera que siempre hay existencia del producto, arreglarlo con un campo de inventario
                              }
                              else
                              {
                                Logger.getLogger(this.getClass().getName()).log(Level.INFO,"Clave de producto INEXISTENTE");  
                              }
            }
        entidades.Customer clte = customerFacade.find(new Integer(id_clte));
        
        // se obtiene el cliente
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                              "Pedido del cliente:" + clte.getId() + ", " + clte.getName() + " por " + fltMontoPedido);
        // 
        // Se genera el customer order
        //
        entidades.CustomerOrder customer_order = new entidades.CustomerOrder();
        customer_order.setCustomerId(clte);
        
        java.util.Date d = new java.util.Date();
        //java.sql.Date sd = new java.sql.Date(d.getTime());

        customer_order.setDateCreated(d);
        customer_order.setAmount(new BigDecimal(fltMontoPedido));
        customer_order.setConfirmationNumber(100);
        
        customerOrderFacade.create(customer_order);
        int num_pedido = customer_order.getId();
        //
        //  Van los items
        //
        entidades.OrderedProductPK oppk;
        for( entidades.OrderedProduct op: lista_orderedProducts)
        {
            oppk = new entidades.OrderedProductPK();
            oppk.setCustomerOrderId(customer_order.getId());
            oppk.setProductId(op.getProduct().getId());
            
            op.setOrderedProductPK(oppk);
            op.setCustomerOrder(customer_order);
            
            orderedProductFacade.create(op);
        }
        
        return num_pedido;
    }
    
    
    
    
}
