# bulldozer

Gather data.

## Available sources

- [Imgur](http://imgur.com/)
- [Google Images](https://www.google.com.ua/imghp)
- [Bing Images](http://bing.com/)
- [Instagram](http://instagram.com)
- [Flickr](http://flickr.com)

## Images

Every image provider has `(get-image)` or `(get-image "keyword")` or both functions.
It returns unified image object with the following properties:

* **:id** "abcdefjhijklomnp"
* **:link** "http://img.com/tarantino.jpg"
* **:width** 600
* **:height** 400
* **:title** "Quentin Tarantino"
* **:preview-link** "http://img.com/tarantino_small.jpg"
* **:preview-width** 90
* **:preview-height** 60
* **:source** :google

Also, there are provider specific properties for image,
which you can inspect by using `(get-raw-image)` or `(get-raw-image "keyword")` functions.

Some provider implements `(quota)` function, which allows to inspect remaining api usage.

Almost all image providers return multiple result per request, so results are cached
internally and refreshed when images are exhausted. Such cache can be invalidated by calling
`(invalidate-cache)` or `(invalidate-cache "keyword")` function.

### Imgur

`(:use [bulldozer.api.imgur])`

* `(get-image)` - random unified image.
* `(get-image "cat")` - random unified image with cat
* `(get-raw-image)` - random imgur-specific image, properties [here](http://api.imgur.com/models/image#model)
* `(get-raw-image "cat")` - random imgur-specific image
* `(link-scale "http://i.imgur.com/abcde.jpg" :s)`  
  Scale imgur image to specific size. Following sizes supported:
  * **:s** (small square 90x90)
  * **:b** (big square 160x160)
  * **:t** (small thumbnail 160x160)
  * **:m** (medium thumbnail 320x320)
  * **:l** (large thumbnail 640x640)
  * **:h** (huge thumbnail 1024x1024)
* `(quota)` - api usage info
  * `(:UserRemaining (quota)) => 491`
  * `(:ClientRemaining (quota)) => 12491`
* `(invalidate-cache)` - clear all pictures saved to random cache
* `(invalidate-cache "cat")` - clear all pictures saved to "cat"-cache 


Imgur provides `12500` requests for client per day, what is
approximately `50K` images/day.

Note, that bulldozer internal `*CLIENT_ID*` is shared and
just for test purposes, so if you want to have `12500`
limit for private usage, register imgur application,
obtain your own `*CLIENT_ID*` and override it in requests:

``` clojure
(binding [*CLIENT_ID* "my-own-client-id"]
  (get-image "cat"))
```

### Google Images

Google Images API is actually deprecated, but still works.

`(:use [bulldozer.api.google])`

* `(get-image "cat")` - random unified image with cat
* `(get-raw-image "cat")` - random google-specific image, properties [here](https://developers.google.com/image-search/v1/devguide#resultobject)
* `(invalidate-cache)` - clear all pictures saved to all keyword caches
* `(invalidate-cache "cat")` - clear all pictures saved to "cat"-cache 

Google provides `100` requests per day and one request returns upto `8` images, what is approximately `800` images/day. 

One search query could return upto `64` results.

### Bing Images

`(:use [bulldozer.api.bing])`

* `(get-image "cat")` - random unified image with cat
* `(get-raw-image "cat")` - random bing-specific image
* `(invalidate-cache)` - clear all pictures saved to all keyword caches
* `(invalidate-cache "cat")` - clear all pictures saved to "cat"-cache
* `(quota)` - remaining number of requests per month 
  
Bing provides `5000` requests per month and one request returns upto `50` images, what is approximately `250K` images/month.

One search query could return upto `1000` results.

### Instagram

`(:use [bulldozer.api.instagram])`

* `(get-image)` - random popular unified image
* `(get-image "cat")` - random unified image tagged as `#cat`
* `(get-raw-image)` - random popular instagram image
* `(get-raw-image "cat")` - random instagram image tagged as `#cat`, json example [here](https://api.instagram.com/v1/tags/snow/media/recent?access_token=174476326.f59def8.d3f7b1318aa14744bbbf421b876e3f46)
* `(get-tags "ukraine")` - return tags associated with *ukraine*
* `(invalidate-cache)` - clear all pictures saved to popular all tagged caches
* `(invalidate-cache "cat")` - clear all pictures saved to "cat"-cache
* `(quota)` - check remaining quota. *Consumes 1 request*
  
Instagram provides `5000` requests per hour and one request returns upto `20` images, what is approximately `100K` images/hour.

### Flickr

`(:use [bulldozer.api.flickr])`

* `(get-image)` - random recent unified image
* `(get-image "cat")` - random unified image with `cat`
* `(get-raw-image)` - random recent flickr image
* `(get-raw-image "cat")` - random flickr image with `cat`
  
Flickr provides `3600` requests per hour and one request returns upto `100` images, what is approximately `360K` images/hour.

## License

Copyright © 2014

Distributed under the Eclipse Public License 1.0
