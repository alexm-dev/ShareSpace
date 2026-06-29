package app.service;

import app.dao.AssetDAO;
import app.dao.CategoryDAO;
import app.dao.SubCategoryDAO;
import app.model.Asset;
import app.model.Category;
import app.model.SubCategory;

import java.util.List;

/**
 * Read-only view over the asset catalog: categories, sub-categories and the
 * assets in them. Browsing-side complement to AssetService, which handles
 * lender-side listing edits.
 */
public class CatalogService {

    private final CategoryDAO categoryDAO;
    private final SubCategoryDAO subCategoryDAO;
    private final AssetDAO assetDAO;

    public CatalogService() {
        this.categoryDAO = new CategoryDAO();
        this.subCategoryDAO = new SubCategoryDAO();
        this.assetDAO = new AssetDAO();
    }

    /**
     * Returns all top-level categories.
     *
     * @return list of categories, empty if none exist
     */
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    /**
     * Returns sub-categories that belong to a given category.
     *
     * @param categoryId the parent category id
     * @return list of sub-categories, empty if none match
     */
    public List<SubCategory> getSubCategoriesByCategoryId(int categoryId) {
        return subCategoryDAO.findByCategoryId(categoryId);
    }

    /**
     * Looks up a single sub-category by id. Used to resolve a sub-category's
     * name when wiring metadata schemas to listings.
     *
     * @param id the sub-category id
     * @return the SubCategory, or null if not found
     */
    public SubCategory getSubCategoryById(int id) {
        return subCategoryDAO.findById(id);
    }

    /**
     * Returns all assets listed under a sub-category.
     *
     * @param subCategoryId the sub-category id
     * @return list of assets, empty if no listings exist
     */
    public List<Asset> getAssetsBySubCategory(int subCategoryId) {
        return assetDAO.findBySubCategoryId(subCategoryId);
    }

    /**
     * Returns every asset in the catalog.
     *
     * @return list of all assets
     */
    public List<Asset> getAllAssets() {
        return assetDAO.findAll();
    }

    /**
     * Gets the full category path for a given sub-category.
     * Is used to display category information in the asset listing view.
     *
     * @param subCategoryId the sub-category id
     * @return the full category path in the format "Category · SubCategory", or just
     * the sub-category name if the parent category is not found, or an empty string if the sub-category is not found
     */
    public String getCategoryPath(int subCategoryId) {
        SubCategory sub = subCategoryDAO.findById(subCategoryId);
        if (sub == null) {
            return "";
        }
        Category category = categoryDAO.findById(sub.getCategoryId());
        if (category == null) {
            return sub.getName();
        }
        return category.getName() + " · " + sub.getName();
    }
}
