package com.example.addressbookspring.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.springframework.context.annotation.Scope;
import jakarta.faces.context.FacesContext;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.addressbookspring.entity.Contact;
import com.example.addressbookspring.service.ContactService;

@Component("contactController")
@Scope("session")
public class ContactController implements Serializable {

    private static final long serialVersionUID = -8163374738411860012L;

    @Autowired
    private ContactService ejbFacade;

    private Contact current;
    private List<Contact> items = null;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public Contact getSelected() {
        if (current == null) {
            current = new Contact();
            selectedItemIndex = -1;
        }
        return current;
    }

    private ContactService getFacade() {
        return ejbFacade;
    }

    public static abstract class PaginationHelper {
        private final int pageSize;
        private int page;

        protected PaginationHelper(int pageSize) { this.pageSize = pageSize; }

        public abstract int getItemsCount();

        public abstract List<Contact> createPageData();

        public int getPageFirstItem() {
        return page*pageSize;
        }

        public int getPageLastItem() {
            int i = getPageFirstItem() + pageSize -1;
            int count = getItemsCount() - 1;
            if (i > count) {
                i = count;
            }
            if (i < 0) {
                i = 0;
            }
            return i;
        }

        public boolean isHasNextPage() {
            return (page+1)*pageSize+1 <= getItemsCount();
        }

        public void nextPage() {
            if (isHasNextPage()) {
                page++;
            }
        }

        public boolean isHasPreviousPage() {
            return page > 0;
        }

        public void previousPage() {
            if (isHasPreviousPage()) {
                page--;
            }
        }

        public int getPageSize() {
            return pageSize;
        }

    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {
                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public List<Contact> createPageData() {
                    int first = getPageFirstItem();
                    int lastExclusive = getPageFirstItem() + getPageSize();
                    return getFacade().findRange(new int[]{ first, lastExclusive });
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView(Contact contact) {
        current = contact;
        selectedItemIndex = getItems().indexOf(contact);
        return "View";
    }

    public String prepareCreate() {
        current = new Contact();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            FacesContext.getCurrentInstance().addMessage(null, 
                new jakarta.faces.application.FacesMessage(
                    ResourceBundle.getBundle("Bundle").getString("ContactCreated")));
            return prepareCreate();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new jakarta.faces.application.FacesMessage(
                    ResourceBundle.getBundle("Bundle").getString("PersistenceErrorOccured")));
            return null;
        }
    }

    public String prepareEdit(Contact contact) {
        current = contact;
        selectedItemIndex = getItems().indexOf(contact);
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            FacesContext.getCurrentInstance().addMessage(null, 
                new jakarta.faces.application.FacesMessage(
                    ResourceBundle.getBundle("Bundle").getString("ContactUpdated")));
            return "View";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new jakarta.faces.application.FacesMessage(
                    ResourceBundle.getBundle("Bundle").getString("PersistenceErrorOccured")));
            return null;
        }
    }

    public String destroy(Contact contact) {
        current = contact;
        selectedItemIndex = getItems().indexOf(contact);
        performDestroy();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            FacesContext.getCurrentInstance().addMessage(null, 
                new jakarta.faces.application.FacesMessage(
                    ResourceBundle.getBundle("Bundle").getString("ContactDeleted")));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new jakarta.faces.application.FacesMessage(
                    ResourceBundle.getBundle("Bundle").getString("PersistenceErrorOccured")));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            selectedItemIndex = count - 1;
            if (getPagination().getPageFirstItem() >= count) {
                getPagination().previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{ selectedItemIndex, selectedItemIndex + 1 }).get(0);
        } else {
            current = null;
        }
    }

    public List<Contact> getItems() {
        if (items == null) {
            items = new ArrayList<>(getPagination().createPageData());
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }
    
    public List<Contact> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Contact> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }
}