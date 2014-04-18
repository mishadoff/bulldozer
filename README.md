# bulldozer

Gather data.

## Available sources

- [Imgur](http://imgur.com/) (In development)
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
(link-scale "http://i.imgur.com/12345.jpg" :m) => "http://i.imgur.com/12345m.jpg"
```

Imgur have some limitations for number of request, what you can inspect by `quota` function

``` clojure
(:UserRemaining (quota)) => 491
(:ClientRemaining (quota)) => 12491
```

## Gist

;; TODO

## License

Copyright © 2013

Distributed under the Eclipse Public License 1.0
