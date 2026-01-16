// package com.example.cache;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.locks.ReentrantReadWriteLock;
// import java.time.LocalDateTime;
// import java.time.LocalDateTime;
// import java.util.*;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.locks.ReentrantReadWriteLock;

// public class LFUCache<K, V>
// {
//     private final int capacity;
//     private final long ttlMinutes;
//     //main storage : key -> cachenode
//     private final Map<K,CacheNode<K,V>> cache;
//     //frequency map : freq -> linkedhashset of cachenodes
//     private final Map<Integer, LinkedHashSet<K>> frequencyMap;
//     private int minFrequency;
   
//     private final ReentrantReadWriteLock lock;

//     public LFUCache(int capacity, long ttlMinutes)
//     {
//         this.capacity = capacity;
//         this.ttlMinutes = ttlMinutes;
//         this.cache = new ConcurrentHashMap<>();
//         this.frequencyMap = new ConcurrentHashMap<>();
//         this.minFrequency = 0;
//         this.lock = new ReentrantReadWriteLock();
//     }

//     private static class CacheNode<K,V>
//     {
//         K key;
//         V value;
//         int frequency;
//         LocalDateTime timestamp;
//         CacheNode(K key, V value)
//         {
//             this.key = key;
//             this.value = value;
//             this.frequency = 1;
//             this.timestamp = LocalDateTime.now();
//         }
//         boolean isExpired(long ttlMinutes)
//         {
//             return LocalDateTime.now().isAfter(this.timestamp.plusMinutes(ttlMinutes));
//         }
//     }
//     public V get(K key)
//     {

//         lock.readLock().lock();
//         try{
//             CacheNode<K,V> node = cache.get(key);
//             if(node == null)
//             {
//                 System.out.println("Cache miss for key: " + key);
//                 return null;
//             }
//             if(node.isExpired(ttlMinutes))
//             {
//                 System.out.println("Cache entry expired for key: " + key);
//                 lock.readLock().unlock();
//                 lock.writeLock().lock();
//                 try
//                 {
//                     // remove(key);
//                     // return null;
//                      resetWindow(node);
//                 } finally
//                 {
//                     lock.readLock().lock();
//                     lock.writeLock().unlock();
//                 }
//             }
//             System.out.println("Cache hit for key: " + key + " with frequency: " + node.frequency);
//             lock.readLock().unlock();
//             lock.writeLock().lock();
//             try
//             {
//                 updateFrequency(node);
//                 return node.value;

//             }finally{
//                 lock.readLock().lock();
//                 lock.writeLock().unlock();
//             }
//         }finally{
//             lock.readLock().unlock();
//         }
//     }
//     private void resetWindow(CacheNode<K,V> node)
// {
//     int oldFreq = node.frequency;
//     LinkedHashSet<K> set = frequencyMap.get(oldFreq);
//     if (set != null) {
//         set.remove(node.key);
//         if (set.isEmpty()) {
//             frequencyMap.remove(oldFreq);
//             if (minFrequency == oldFreq) {
//                 minFrequency = 1;
//             }
//         }
//     }

//     node.frequency = 1;
//     node.timestamp = LocalDateTime.now();
//     frequencyMap.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(node.key);
// }

//     //put value to key
//     public void put(K key, V value)
//     {
//         if(capacity <=0) return;
//         lock.writeLock().lock();
//         try
//         {
//             if(cache.containsKey(key))
//             {
//                 CacheNode<K,V> node = cache.get(key);
//                 // node.value = value;
//                 // node.timestamp = LocalDateTime.now();
//                 // updateFrequency(node);
               
//                 boolean expired = node.isExpired(ttlMinutes);

//                 node.value = value;
//                 node.timestamp = LocalDateTime.now();

//                 if (expired) {
//                     resetWindow(node);
//                 } else {
//                     updateFrequency(node);
//                 }
//                 System.out.println("Updated existing key: " + key);
//                 return;
//             }
//             if(cache.size() >= capacity)
//             {
//                 evictLFU();

//             }
           
//             CacheNode<K,V> newNode = new CacheNode<>(key,value);
//             cache.put(key,newNode);
//             frequencyMap.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
//             minFrequency = 1;
//             System.out.println("Added new key: " + key);

//         }finally{
//             lock.writeLock().unlock();
//         }
//     }
//         public void remove(K key)
//         {
//             lock.writeLock().lock();
//             try
//             {
//                 CacheNode<K,V> node = cache.remove(key);
//                 if(node != null)
//                 {
//                     LinkedHashSet<K> keys = frequencyMap.get(node.frequency);
//                     if(keys!= null)
//                     {
//                         keys.remove(key);
//                         if(keys.isEmpty())
//                         {
//                             frequencyMap.remove(node.frequency);
                             

//                     }
//                 }
//                 System.out.println("Removed key: " + key);
//             }
//             }finally{
//                 lock.writeLock().unlock();
//             }



//         }
//         public void clear()
//         {
//             lock.writeLock().lock();
//             try
//             {
//                 int size = cache.size();
//                 cache.clear();
//                 frequencyMap.clear();
//                 minFrequency = 0;
//                 System.out.println("Cleared cache, removed " + size + " entries.");

//             }finally{
//                 lock.writeLock().unlock();
//             }
//         }
//         public int size()
//         {
//             lock.readLock().lock();
//             try
//             {
//                 return cache.size();

//             }finally{
//                 lock.readLock().unlock();
//             }
//         }
//         public Map<String,Object> getStats()
//         {
//             lock.readLock().lock();
//             try
//             {
//                 Map<String,Object> stats = new HashMap<>();
//                 stats.put("size", cache.size());
//                 stats.put("capacity", capacity);
//                 stats.put("minFrequency", minFrequency);
//                 stats.put("ttlMinutes", ttlMinutes);
//                 Map<Integer,Integer> freqDistribution = new HashMap<>();
//                 for(Map.Entry<Integer,LinkedHashSet<K>> entry : frequencyMap.entrySet())
//                 {
//                     freqDistribution.put(entry.getKey(), entry.getValue().size());
//                 }
//                 stats.put("frequencyDistribution", freqDistribution);
//                 return stats;
//             }finally{
//                 lock.readLock().unlock();
//             }
//         }
//     private void updateFrequency(CacheNode<K,V> node)
//     {
//         int oldFreq = node.frequency;
//         LinkedHashSet<K> oldKeys = frequencyMap.get(oldFreq);
//         oldKeys.remove(node.key);
//         if(oldKeys.isEmpty())
//         {
//             frequencyMap.remove(oldFreq);
//             if(oldFreq == minFrequency)
//             {
//                 minFrequency++;
//             }
//         }
//         node.frequency++;
//         frequencyMap.computeIfAbsent(node.frequency, k -> new LinkedHashSet<>()).add(node.key);
//     }
//     private void evictLFU()
//     {
//         LinkedHashSet<K> keys = frequencyMap.get(minFrequency);
//         if(keys == null || keys.isEmpty())
//         {
//             System.out.println("No keys to evict");
//             return;
//         }
//         K evictKey = keys.iterator().next();
//         keys.remove(evictKey);
//         if(keys.isEmpty())
//         {
//             frequencyMap.remove(minFrequency);
//         }
//         CacheNode<K,V> evictedNode = cache.remove(evictKey);
//         System.out.println("Evicted key: " + evictKey + " with frequency: " + evictedNode.frequency);
//     }
    

//     public int cleanupExpired()
//     {
//         lock.writeLock().lock();
//         try
//         {
//             List<K> expiredKeys = new ArrayList<>();
//             for(Map.Entry<K,CacheNode<K,V>> entry : cache.entrySet())
//             {
//                 if(entry.getValue().isExpired(ttlMinutes))
//                 {
//                     expiredKeys.add(entry.getKey());
//                 }
//             }
//             for(K key : expiredKeys)
//             {
//                 remove(key);
//             }
//             if(!expiredKeys.isEmpty())
//             {
//                 System.out.println("Cleaned up expired keys: " + expiredKeys);
//             }
//             return expiredKeys.size();
//         }
//         finally
//         {
//             lock.writeLock().unlock();
//         }
//     }
    
// }
