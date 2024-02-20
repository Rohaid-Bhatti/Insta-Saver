// injectImage.js

javascript: (function () {

console.log("TESTING_JS: " +  " my js file is running " );

    var isClickCooling = true;
    console.log("加载ins解析代码>>>>>>>>>");
    /*function warn(tag, msg = "") {
        ADAPTATION_HOLDER.warn(tag, msg);
    }
    function receiveJsParseResult(jsonResult) {
        ADAPTATION_HOLDER.receiveJsParseResult(jsonResult);
    }*/

    function isEmpty(obj) {
        if (typeof obj == "undefined" || obj == null || obj == "") {
            return true;
        } else {
            return false;
        }
    }

    var createBtn = function (top) {
        var div = document.createElement("DIV");
        div.className = "ins_dl";
//        div.setAttribute("style", "position:absolute;top:" + top + ";left:0px;width: 50px; height: 50px; z-index:9999");
        div.setAttribute("style", "position: absolute; top:" + top + "; right: 20px; width: 50px; height: 50px; z-index: 9999");
        div.innerHTML = '<img style="position:absolute;top:15px;bottom:0px;left:10px;right:0px;margin:auto;;width: 50px; height: 50px; object-fit: fill; pointer-events: none" src="data:image/png;base64,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" />';
        return div;
    };

    var tagMedia = function (rootNode, t, parentNode, isStory, isPost) {

        var tagName = rootNode.tagName;
        var targetContainer = parentNode;
        //已经注入的标签无须再次处理
        if (targetContainer.getAttribute("tag") == 1) {
            return;
        }
        var top = "10px";
        if (isStory) {
            top = "100px";
        }
        var div = createBtn(top);
        //注入下载按钮
        div.onclick = function (event) {
        console.log("BUTTON_CLICK: " +  "my button is clicked" );


         if (!isClickCooling) {

                return;
            }

      /*ADAPTATION_HOLDER.clickDownloadButtonEvent(window.location.host)
               isClickCooling = false
               setTimeout(function () {
                   isClickCooling = true;
               }, 500);

            event.stopPropagation();*/

            var namePrefix = "ins";
            if (t != null) {
                namePrefix = t;
            }

            var data = null;
            var type = "jpg";


             var videoTag = parentNode.getElementsByTagName('video')[0];
             console.log("TESTING_JS: " +  "videoTag " +videoTag );


             if (videoTag != null) {
                type = "mp4";

                var videoUrl = videoTag.getAttribute('src');
                console.log("TESTING_JS: " +  "videoTag " +videoUrl );

                if (!videoUrl) {
                    var sources = videoTag.getElementsByTagName("source");
                    if (sources.length > 0) {
                        videoUrl = sources[0].src;
                    }
                }

                 console.log("TESTING_JS: " +  "sources added " +videoUrl );
                 Android.downloadMedia(videoUrl);

                var thumbnailUrl = videoTag.src;
                console.log("TESTING_JS: " +  "thumbnailUrl poster" +videoTag.scr );

               if (thumbnailUrl && thumbnailUrl.indexOf("base64") >= 0) {
                    var imgs = parentNode.getElementsByTagName("IMG");
                    var thumbnailUrl = "";
                    if (imgs.length > 0) {
                        thumbnailUrl = imgs[0].src;
                    }
                }

                console.log("TESTING_JS: " +  "thumbnailUrl = imgs[0].src   "+ thumbnailUrl );

                if (videoUrl && !videoUrl.startsWith("blob") && /[^\s]/.test(videoUrl)) {
                    var timestamp = Date.now();
                    data = {
                        name: timestamp + ".mp4",
                        sourceUrl: location.href,
                        quality: "sd",
                        fromUrl: location.href,
                        thumbnailUrl: thumbnailUrl,
                        mediaUrlList: [videoUrl]
                    };

//                var anchorTags = document.getElementsByClassName('x1i10hfl xjbqb8w x1ejq31n xd10rxx x1sy0etr x17r0tee x972fbf xcfux6l x1qhh985 xm0m39n x9f619 x1ypdohk xt0psk2 xe8uvvx xdj266r x11i5rnm xat24cr x1mh8g0r xexx8yu x4uap5 x18d9i69 xkhd6sd x16tdsg8 x1hl2dhg xggy1nq x1a2a7pz _a6hd');
                var anchorTags = document.getElementsByTagName('a')[0];

//                    if (anchorTags.length > 0) {
//                        var anchorTag = anchorTags[0];
                    var hrefValue = anchorTags.getAttribute('href');
                    console.log('Href value:', hrefValue);
//                    } else {
//                        console.log('Anchor tag with the specified class name not found.');
//                    }

                   console.log("videoData thumbnailUrl: " + data.thumbnailUrl + " mediaUrlList: " + data.mediaUrlList);
                }

                else {
                    warn("INS_VIDEO_URL_INVALID", videoUrl);
                    console.log("非法ins地址::: " + videoUrl);
                    return;
                }
              }

             else {
                var imgs = parentNode.getElementsByTagName("img");
                if (imgs.length > 0) {
                    if (isEmpty(imgs[0].className) && imgs.length > 1) {
                        imgUrl = imgs[1].src;
                    } else {
                        imgUrl = imgs[0].src;
                    }

                    console.log("imgUrl: " + imgUrl);
                    Android.downloadMedia(imgUrl);

                    if (!isEmpty(imgUrl)) {
                        var timestamp = Date.now();
                        data = {
                            name: timestamp + ".jpg",
                            sourceUrl: location.href,
                            quality: "sd",
                            thumbnailUrl: imgUrl,
                            fromUrl: location.href,
                            mediaUrlList: [imgUrl]
                        };
                        if (data != null) {
                            console.log("imgData thumbnailUrl: " + data.thumbnailUrl + " mediaUrlList: " + data.mediaUrlList);
                        }
                    }
                }
            }
            if (data != null) {
                var resultData = {
                    'status': 'time_line',
                    'fromType': 'ins',
                    'showDialog': true,
                    'parseType': type,
                    'dataList': [data],
                    'fromUrl': location.href,
                    'dataSource': 'timeline===ins'
                };
//                ADAPTATION_HOLDER.setParseBtnType("adapter", "websiteBtn", "insta_script.js");
//                receiveJsParseResult(JSON.stringify(resultData));
            } else {
                warn("INS_NO_DATA");
            }
        };
        targetContainer.appendChild(div);
        targetContainer.setAttribute("tag", 1);
    };

    function tagRealVideo(videoSrc, videoParent) {


      console.log("TESTING_FUNC: " +  "tagRealVideo" );

    	var targetContainer = videoParent;


    	if (isEmpty(videoSrc) || targetContainer.getAttribute("tag") == 1) {
    		return;
    	}
    	div = createBtn("40px");
    	div.onclick = function (event) {
    		if (!isClickCooling) {
    			return;
    		}
    		isClickCooling = false;
    		setTimeout(function () {
    			isClickCooling = true;
    		}, 1500);
    		event.stopPropagation();
    		// 采集用户信息
    		var videoUrl = videoSrc;
    		console.log("insta_script videoUrl: " + videoUrl);
            var timestamp = Date.now();
            data = {
                name: timestamp + ".mp4",
                sourceUrl: location.href,
                quality: "sd",
                fromUrl: location.href,
                mediaUrlList: [videoUrl]
            };
            var resultData = {
                'status': 'time_line',
                'fromType': 'ins',
                'showDialog': true,
                'parseType': "mp4",
                'dataList': [data],
                'fromUrl': location.href,
                'dataSource': 'timeline===ins'
            };
//            ADAPTATION_HOLDER.setParseBtnType("adapter", "websiteBtn", "insta_script.reel.js");
//            receiveJsParseResult(JSON.stringify(resultData));
    	};
    	targetContainer.appendChild(div);
    	targetContainer.setAttribute("tag", 1);
    }


    var findAllMedias = function (target, from) {
    console.log("TESTING_FUNC: " +  "findAllMedias" );

	var currUrl = window.location.href;
	console.log("currUrl: " + currUrl);
	if (/https?:\/\/.*instagram\.com\/stories\.*\//.test(currUrl)) {

		if (target.className == "section") {

			handleSection(target);
		} else {
			var sections = target.getElementsByTagName("section");
			if (sections.length > 0) {
				for (var i = 0; i < sections.length; i++) {
					handleSection(sections[i]);
				}
			}
		}
	} else {
		if (/https?:\/\/.*instagram\.com\/p\/(?!.*chaining=true)/.test(currUrl)) {
			//story页面 查找所有的section标签
			if (target.className == "section") {
				handleArticle(target, true);
			} else {
				var sections = target.getElementsByTagName("section");
				if (sections.length > 0) {
					for (var i = 0; i < sections.length; i++) {
						var sectionE = sections[i];
						var mainE = sectionE.children[0];
						if (mainE != null) {
							var navE = mainE.children[0];
							if (navE != null && navE.tagName == "NAV") {
								var articleSameE = mainE.children[1];
								if (articleSameE != null) {
									handleArticle(articleSameE, true);
								}
							}
						}
					}
				}
			}
		}

		if (target.className == "article") {
			handleArticle(target, false);
		} else {
			var articles = target.getElementsByTagName("article");
			if (articles.length > 0) {
				var hasMedia = true;
				if (articles.length > 4) {
					hasMedia = false;
				}
				for (var i = 0; i < articles.length; i++) {
					var isValid = handleArticle(articles[i], false);
					if (isValid) {
						hasMedia = true;
					}
				}
				// if (!hasMedia) {
				// 	reportError("has article no photo/video");
				// }
			} else {
				if (/https?:\/\/.*instagram\.com\/explore\.*\//.test(currUrl)) {
				} else {
				    var mainE = document.getElementsByTagName("main")[0];
                    if (mainE != null) {
                        var mainChildOne = mainE.children[0];
                        if (mainChildOne != null) {
                            var listParent = mainChildOne.children[0];
                            if (listParent != null) {
                                var list = listParent.childNodes;
                                for (var i = 0; i < list.length; i++) {
                                    var mediaItemE = list[i];
                                    var videoInfoPPE = mediaItemE.children[0];
                                    if (videoInfoPPE != null) {
                                        var videoE = videoInfoPPE.getElementsByTagName("video")[0]; // 视频信息
                                        if (videoE != null) {
                                            tagRealVideo(videoE.src, mediaItemE, videoInfoPPE);
                                        }
                                    }
                                }
                            }
                        }
                    }
				}
			}
		}
	}
};

    //获取所有有效标签
//    var findAllMedias = function (target, from) {
//    	var currUrl = window.location.href;
//    	if (/https?:\/\/.*instagram\.com\/stories\.*\//.test(currUrl)) {
//    		//story页面 查找所有的section标签
//    		if (target.className == "section") {
//    			handleSection(target)
//    		} else {
//    			var sections = target.getElementsByTagName("section")
//    			if (sections.length > 0) {
//    				for (var i = 0; i < sections.length; i++) {
//    					handleSection(sections[i])
//    				}
//    			}
//    		}
//    	} else {
//    		//找到所有的article标签
//    		if (target.className == "article") {
//    			handleArticle(target)
//    		} else {
//    			var articles = target.getElementsByTagName("article")
//    			if (articles.length > 0) {
//    				var hasMedia = true;
//    				if (articles.length > 4) {
//    					hasMedia = false; // 需要检测
//    				}
//    				for (var i = 0; i < articles.length; i++) {
//    					var isValid = handleArticle(articles[i])
//    					if (isValid) {
//    						hasMedia = true;
//    					}
//    				}
//    			} else {
//    				// reel页面
//    				var mainE = document.getElementsByTagName("main")[0]
//    				if (mainE != null) {
//    					var mainChildOne = mainE.children[0]
//    					if (mainChildOne != null) {
//    						var listParent = mainChildOne.children[0]
//    						if (listParent != null) {
//    							var list = listParent.childNodes
//    							for (var i = 0; i < list.length; i++) {
//    								var mediaItemE = list[i];
//    								var videoInfoPPE = mediaItemE.children[0]
//    								if (videoInfoPPE != null) {
//    									var videoE = videoInfoPPE.getElementsByTagName("video")[0] // 视频信息
//    									if(videoE != null){
//    										tagRealVideo(videoE.src, mediaItemE, videoInfoPPE)
//    									}
//    								}
//    							}
//    						}
//    					}
//    				}
//    			}
//    		}
//    	}
//    }

    var handleArticle = function (article, isPost) {
       var isValid = false;
       	var ulE = article.getElementsByTagName("ul")[0];
       	if (ulE != null) {
       		var liEs = article.getElementsByTagName("li");
       		if (liEs.length > 0) {
       			for (var idx = 0; idx < liEs.length; idx++) {
       				var liE = liEs[idx];
       				var imgE = liE.getElementsByTagName("img")[0];
       				if (imgE != null) {
       					isValid = true;
       					tagMedia(article.children[0], imgE, liE, false, isPost);
       				} else {
       					var videoE = liE.getElementsByTagName("video")[0];
       					if (videoE != null) {
       						isValid = true;
       						tagMedia(article.children[0], videoE, liE, false, isPost);
       					}
       				}
       			}
       		}
       	} else {
       		var rootNode = article.children[0];
       		if (rootNode != null) {
       			var contentRoot = rootNode.children[1];
       			if (contentRoot != null) {
       				var imgs = contentRoot.getElementsByTagName("img");
       				var imgE = imgs[0];
       				if (imgs.length == 1 && imgE.parentNode.className != "ins_dl") {
       					tagMedia(rootNode, imgs[0], imgs[0].parentNode, false, isPost);
       				} else {
       					var videos = contentRoot.getElementsByTagName("video");
       					if (videos.length == 1) {
       						tagMedia(rootNode, videos[0], videos[0].parentNode, false, isPost);
       					}
       				}
       			}
       		}
       	}
        return isValid;
    };

    var handleSection = function (section) {
        var videoE = section.getElementsByTagName("video")[0];
        if (videoE != null) {
            tagMedia(section, videoE, videoE.parentNode, true, false);
        } else {
            var imgE = section.getElementsByTagName("img")[0];
            if (imgE != null) {
                tagMedia(section, imgE, imgE.parentNode, true, false);
            }
        }
    };

    var observerOptions = {
        childList: true, //story需要添加
        attributes: true, // 观察属性变动
        subtree: true,// 观察后代节点，默认为 false
        attributeFilter: ['style', 'class']
    };

    var observer = new MutationObserver(function (mutations, observer) {
        mutations.forEach((mutation) => {
            switch (mutation.type) {
                case 'childList':
                    mutation.addedNodes.forEach((node) => {
                        if ((node.tagName == "DIV" || node.tagName == "SECTION" || node.tagName == "LI" || node.tagName == "ARTICLE" || node.tagName == "IMG") && node.className != "ins_dl") {
                            findAllMedias(document, "childList");
                        }
                    });
                    break;
//                case 'attributes':
//                    var node = mutation.target;
//                    if ((node.tagName == "DIV" || node.tagName == "SECTION") && node.className != "ins_dl") {
//                        findAllMedias(document, "attributes")
//                    }
//                    break;
            }
        });
    });
    console.log(" ======== 开始监听元素变化 ===== ");
    findAllMedias(document, "all");
    observer.observe(document.body, observerOptions);
    console.log("完成加载 <<<<<<<<<<<<<<<<<<");
}())/*()*/;