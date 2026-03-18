namespace Server3.Interfaces
{
    public interface IRepository<T> where T : class
    {
        Task<T> GetFirstAsync(int id);
        Task<List<T>> GetAllAsync();
        Task CreateAsync(T entity);
        Task UpdateAsync(T entity);
        Task DeleteAsync(T entity);
        IQueryable<T> GetQuery();
        Task SaveChangesAsync();
    }
}
