# bulldozer

Gather data.

## Available sources

- [Imgur](http://imgur.com/) (In development)
- [Google Images](https://www.google.com.ua/imghp) (In development)
- [Gist](http://gist.github.com/) (In development)

## Imgur

### Usage

`(:use [bulldozer.api.imgur])`

You can get some random image link 

``` clojure
(get-image) => {:link "http://i.imgur.com/abcdef.png"
	            :width 600
				:height 480
				:title "Image"}
```

or random image by keyword

``` clojure
(get-image "cat") => {:link "http://i.imgur.com/catdog.png"
	                  :width 1200
					  :height 1000
					  :title "Cat kills Dog"}
```

Full list of image properties [here](http://api.imgur.com/models/image#model)

See also: [Unification](#Unification)

### Additional features

To simplify scaling for big pictures, imgur have some conventions to links they provide. Just add <suffix> to the end of filename.

- **s** (small square 90x90)
- **b** (big square 160x160)
- **t** (small thumbnail 160x160)
- **m** (medium thumbnail 320x320)
- **l** (large thumbnail 640x640)
- **h** (huge thumbnail 1024x1024)

Though, there is a function to simplify that

``` clojure
(link-scale "http://i.imgur.com/12345.jpg" :m)
=> "http://i.imgur.com/12345m.jpg"
```

Imgur have some limitations for number of request, what you can inspect by `quota` function

``` clojure
(:UserRemaining (quota)) => 491
(:ClientRemaining (quota)) => 12491
```

;; TODO provide client key

## Google Images

`(:use [bulldozer.api.google])`

Google Images api is deprecated but works.
You can get upto 64 results per search query.
Results cached.

``` clojure
(get-image "tarantino") => {:url "http://imdb.com/tarantino.jpg"
                            :width "640"
							:height "480"
							:title "Pulp Fiction in action"}
```

Full list of image properties [here](https://developers.google.com/image-search/v1/devguide#resultobject)

See also: [Unification](#Unification)

## Gist

;; TODO

## Unification

Every service provides some sort of media result (image, audio, video, etc.) These results may be unified to specific format to simplify usage if more than one service used.

In addition, unification provides standard types for common values despite of what type returned by service.

### Image

`(unify (get-image "cat")`

Image is just a map with following properties

* **:id** "abcdefjhijklomnp"
* **:link** "http://img.com/tarantino.jpg"
* **:width** 600
* **:height** 400
* **:title** "Quentin Tarantino"
* **:content** "Director works on his new film"
* **:preview-link** "http://img.com/tarantino_small.jpg"
* **:preview-width** 90
* **:preview-height** 60
* **:source** :google

## License

Copyright © 2014

Distributed under the Eclipse Public License 1.0
