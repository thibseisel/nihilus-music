package fr.nihilus.mymusic.media;

import android.support.annotation.NonNull;

import dagger.Binds;
import dagger.Module;
import fr.nihilus.mymusic.media.cache.LruMusicCache;
import fr.nihilus.mymusic.media.cache.MusicCache;
import fr.nihilus.mymusic.media.repo.CachedMusicRepository;
import fr.nihilus.mymusic.media.repo.MusicRepository;
import fr.nihilus.mymusic.media.source.MediaStoreMusicDao;
import fr.nihilus.mymusic.media.source.MusicDao;

/**
 * Define relations in the object graph for the "media" group of features.
 * Those classes are bound to the application context and are instantiated only once.
 *
 * <p>Binds annotatetd methods are used by Dagger to know which implementation
 * should be injected when asking for an abstract type.
 */
@Module
public abstract class MediaModule {

    @Binds
    abstract MusicCache bindsMusicCache(@NonNull LruMusicCache cacheImpl);

    @Binds
    abstract MusicDao bindsMusicDao(@NonNull MediaStoreMusicDao daoImpl);

    @Binds
    abstract MusicRepository bindsMusicRepository(@NonNull CachedMusicRepository repositoryImpl);
}
