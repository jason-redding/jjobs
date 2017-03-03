(function($, document, window) {
	if (window['j-was-here'] === true) {
		return;
	}
	window['j-was-here'] = true;

	function show(type) {
		var count = 0;
		for (var key in window.methods) {
			var $row = $('[id="' + key + '"]');
			if ((window.methods[key] & type) !== 0) {
				$row.show();
				$row.removeClass('rowColor altColor').addClass((count++ % 2) ? 'rowColor' : 'altColor');
			} else {
				$row.hide();
			}
		}
		updateTabs(type);
	}

	function updateTabs(type) {
		for (var value in window.tabs) {
			var $sNode = $('[id="' + window.tabs[value][0] + '"]');
			var $spanNode = $sNode.children(':first-child').first();
			$sNode.removeClass('tableTab activeTableTab').addClass((parseInt(value) === type) ? 'activeTableTab' : 'tableTab');
			if (parseInt(value) === type) {
//				$spanNode[0].innerHTML = window.tabs[value][1];
				$spanNode.find('a').wrapInner('<span/>').children('span').unwrap('a').unwrap('span');
//				$spanNode.text(window.tabs[value][1]);
			} else {
				if ($spanNode.find('a').length === 0) {
//					$spanNode[0].innerHTML = "<a href=\"javascript:show(" + value + ");\">" + window.tabs[value][1] + "</a>";
					$spanNode.wrapInner('<a href="javascript:show(' + value + ');"></a>');
				}
			}
		}
	}
	window.updateTabs = updateTabs;
	window.show = show;




	function initPrismLineNumbers() {
		if (typeof self === 'undefined' || !self.Prism || !self.document) {
			return;
		}

		Prism.hooks.add('complete', function(env) {
			if (!env.code) {
				return;
			}

			// works only for <code> wrapped inside <pre> (not inline)
			var pre = env.element.parentNode;
			var clsReg = /\s*\bline-numbers\b\s*/;
			if (
			!pre || !/pre/i.test(pre.nodeName) ||
			// Abort only if nor the <pre> nor the <code> have the class
			(!clsReg.test(pre.className) && !clsReg.test(env.element.className))
			) {
				return;
			}

			if (env.element.querySelector(".line-numbers-rows")) {
				// Abort if line numbers already exists
				return;
			}

			if (clsReg.test(env.element.className)) {
				// Remove the class "line-numbers" from the <code>
				env.element.className = env.element.className.replace(clsReg, '');
			}
			if (!clsReg.test(pre.className)) {
				// Add the class "line-numbers" to the <pre>
				pre.className += ' line-numbers';
			}

			var match = env.code.match(/\n(?!$)/g);
			var linesNum = match ? match.length + 1 : 1;
			var lineNumbersWrapper;

			var lines = new Array(linesNum + 1);
			lines = lines.join('<span></span>');

			lineNumbersWrapper = document.createElement('span');
			lineNumbersWrapper.setAttribute('aria-hidden', 'true');
			lineNumbersWrapper.className = 'line-numbers-rows';
			lineNumbersWrapper.innerHTML = lines;

			if (pre.hasAttribute('data-start')) {
				pre.style.counterReset = 'linenumber ' + (parseInt(pre.getAttribute('data-start'), 10) - 1);
			}
			env.element.appendChild(lineNumbersWrapper);
		});
	}


	$(function() {
		initThemePicker([
			'Base',
			'Black Tie',
			'Blitzer',
			'Cupertino',
			'Dark Hive',
			'Dot Luv',
			'Eggplant',
			'Excite Bike',
			'Flick',
			'Hot Sneaks',
			'Humanity',
			'Le Frog',
			'Mint Choc',
			'Overcast',
			'Pepper Grinder',
			'Redmond',
			'Smoothness',
			'South Street',
			'Start',
			'Sunny',
			'Swanky Purse',
			'Trontastic',
			'UI Darkness',
			'UI Lightness',
			'Vader'
		], 'Redmond');

		var defaultPrismTheme = window.localStorage.getItem('prism-theme');
		initJCode();
		defaultPrismTheme = defaultPrismTheme || 'coy';
		loadPrismTheme(defaultPrismTheme)
		.then(function() {
			initPrismLineNumbers();
			Prism.hooks.add('complete', function(env) {
				if (!env.code) {
					return;
				}
				$('.enhanced-code .code-button').trigger('updateAutoWidth');
			});
		});
	});

	function createPrismThemePicker(themes, defaultTheme) {
		themes = themes || ['Coy', 'Dark', 'Default', 'Funky', 'Okaidia', 'Solarized Light', 'Twilight'];
		if (arguments.length < 2) {
			defaultTheme = themes[0];
		}
		var lastTheme = window.localStorage.getItem('prism-theme');
		if (lastTheme) {
			defaultTheme = lastTheme;
		}
		defaultTheme = $.trim(defaultTheme).toLowerCase().replace(/\s+/g, '-');

		var $select = $('<select/>');
		$.each(themes, function(index, value) {
			var v = $.trim(value).toLowerCase().replace(/\s+/g, '-');
			var $option = $('<option/>').text(value).val(v);
			if (defaultTheme === v) {
				$option.attr('selected', 'selected').prop('selected', true);
			}
			$option.appendTo($select);
		});
		$select
		.selectmenu({
			'classes': {
				'ui-selectmenu': 'prism-theme-picker'
			},
			'position': {
				'my': 'right top',
				'at': 'right bottom',
				'collision': 'flipfit'
			},
			'open': function(event, ui) {
				var $this = $(this);
				var $menu = $this.selectmenu('menuWidget');
				var $selectMenu = $menu.closest('.ui-selectmenu-menu');
				// remove ui-corner classes from menu container
				$.each($.trim($selectMenu.attr('class')).split(/\s+/), function(index, value) {
					if (/^ui-corner-/.test(value)) {
						$selectMenu.removeClass(value);
					}
				});
				// copy ui-corner classes from menu to its container
				$.each($.trim($menu.attr('class')).split(/\s+/), function(index, value) {
					if (/^ui-corner-/.test(value)) {
						$selectMenu.addClass(value);
					}
				});
			},
			'change': function(event, ui) {
				var $this = $(this);
				var v = $this.val();
				window.localStorage.setItem('prism-theme', v);
				$('.prism-theme-picker[aria-owns]', document)
				.each(function() {
					var $uiSelect = $(this);
					var $select = $('[id="' + $.trim($uiSelect.attr('aria-owns')).replace(/-menu$/, '') + '"]', document);
					$select
					.not($this)
					.children('option[value="' + v + '"]:first')
					.prop('selected', true)
					.end()
					.selectmenu('refresh');
				});
				loadPrismTheme(v);
			}
		});
		var $widget = $select.selectmenu('widget');
		$widget.addClass('prism-theme-picker');
		var $r = $();
		$r = $r.add($select).add($widget);
		return $r;
	}

	function initJCode() {
		var defaultMaximized = true;
		var $r = $('pre code[class*="jcode-"]').each(function() {
			var $this = $(this);
			var lang;
			$.each($this.attr('class').split(/\s+/), function(index, value) {
				if (/^jcode-/.test(value)) {
					lang = value.replace(/^jcode-/, '');
					return false;
				}
			});
			var $pre = $this.closest('pre');
			if ($.type(lang) === 'string') {
				$pre.addClass('language-' + lang);
			}
			$pre
			.addClass('line-numbers');

			var $wrapper = $pre.wrap('<div/>')
			.parent()
			.addClass('enhanced-code');
			$wrapper[defaultMaximized ? 'removeClass' : 'addClass']('auto-width');

			var $prismThemePicker = createPrismThemePicker()
			.appendTo($wrapper);

			var $codeButton = $('<button/>')
			.text((defaultMaximized ? 'Auto-Width' : 'Full-Width'))
			.addClass('code-button')
			.appendTo($wrapper);
			$codeButton.button({
				'icon': 'ui-icon-arrowthickstop-1-' + (defaultMaximized ? 'w' : 'e'),
				'showLabel': false
			})
			.on('updateAutoWidth', function(event) {
				var $this = $(this);
				var $wrapper = $this.closest('.enhanced-code');
				var isAutoWidth = ($wrapper.is('.auto-width'));
				$this
				.text((isAutoWidth ? 'Full-Width' : 'Auto-Width'))
				.attr('title', $this.text());
				$this.button('option', 'icon', 'ui-icon-arrowthickstop-1-' + (isAutoWidth ? 'e' : 'w'));
//				var rPadding = parseFloat($.trim($wrapper.css('padding-right')).replace(/\D*$/, ''));
				var autoWidth = $wrapper.css({
					'display': 'table',
					'max-width': '',
					'width': 'auto'
				}).width();
//				if ($.isNumeric(rPadding)) {
//					autoWidth += rPadding;
//				}
				var fullWidth = $wrapper
				.css({
					'display': 'block'
				}).outerWidth(true);
				$wrapper.css({
					'display': 'block',
					'max-width': (isAutoWidth ? Math.min(autoWidth, fullWidth) : ''),
					'width': ''
				});
//				console.log('autoWidth=' + autoWidth + '\nfullWidth=' + fullWidth);
			})
			.on('click', function(event) {
				var $this = $(this);
				var $wrapper = $this.closest('.enhanced-code');
				var isMaxWidth = ($wrapper.is('.auto-width') === false);
				var autoWidth;
				$wrapper.stop(true, true);
				$wrapper.toggleClass('auto-width');
//				$this.text((isMaxWidth ? 'Maximize' : 'Restore') + ' Width');
//				$this.attr('title', $this.text());
				var rPadding = parseFloat($.trim($wrapper.css('padding-right')).replace(/\D*$/, ''));
				if (isMaxWidth) {
					var maxWidth = $wrapper.outerWidth(true);
					autoWidth = $wrapper
					.css({
						'display': 'table',
						'max-width': '',
						'width': 'auto'
					}).width();
					maxWidth = Math.min(autoWidth, maxWidth);
					$wrapper
					.css({
						'display': 'block',
//						'padding-right': (rPadding === 0 ? '' : rPadding),
						'width': '100%'
					})
					.animate({
						'width': maxWidth
					},
					{
						'complete': function() {
							$this.trigger('updateAutoWidth');
						},
						'duration': 600
					});
				} else {
					var minWidth = $wrapper.width();
					autoWidth = $wrapper
					.css({
						'display': 'table',
						'max-width': '',
						'width': 'auto'
					})
					.width();
					if ($.isNumeric(rPadding)) {
						minWidth += rPadding;
					}
					minWidth = Math.min(minWidth, autoWidth);
					$wrapper
					.css({
						'display': 'block',
//						'padding-right': (rPadding === 0 ? '' : rPadding),
						'width': minWidth
					})
					.animate({
						'width': '100%'
					},
					{
						'complete': function() {
							$this.trigger('updateAutoWidth');
						},
						'duration': 600
					});
				}
			})
			.trigger('updateAutoWidth')
			;
		});
		return $r;
	}

	$(function() {
		$('.jtabs').each(function() {
			var $tabs = $(this);
			if ($.isFunction($tabs.tabs)) {
				$tabs
				.tabs();
				var tabsOrientation;
				if ($tabs.is('[id]')) {
					tabsOrientation = window.localStorage.getItem('tabs-orientation#' + $tabs.attr('id'));
				}
				tabsOrientation = (tabsOrientation || 'horizontal');
				$tabs[(tabsOrientation === 'vertical') ? 'addClass' : 'removeClass']('ui-tabs-vertical');

				var $tabRotator = $('<button/>')
				.addClass('tabs-rotator')
				.text('Rotate Tabs')
				.button({
					'icon': 'ui-icon-arrowthickstop-1-w',
					'showLabel': false
				})
				.on('update-orientation', function(event) {
					var $this = $(this);
					var $tabs = $this.closest('.ui-tabs');
					var isVertical = $tabs.is('.ui-tabs-vertical');
					$this.button('option', 'icon', 'ui-icon-arrowthickstop-1-' + (isVertical ? 'n' : 'w'));
					$tabs.find('> .ui-tabs-nav > li')
					[isVertical ? 'removeClass' : 'addClass']('ui-corner-top')
					[isVertical ? 'addClass' : 'removeClass']('ui-corner-left')
					.find('> .ui-tabs-anchor')
					.addClass('ui-corner-all-inherit');
				})
				.on('click', function(event) {
					var $this = $(this);
					var $tabs = $this.closest('.ui-tabs');
					$tabs.toggleClass('ui-tabs-vertical');
					if ($tabs.is('[id]')) {
						window.localStorage.setItem('tabs-orientation#' + $tabs.attr('id'), ($tabs.is('.ui-tabs-vertical') ? 'vertical' : 'horizontal'));
					}
					$this.triggerHandler('update-orientation');
				})
				.appendTo($tabs.find('> .ui-tabs-nav'));
				$tabRotator.triggerHandler('update-orientation');
			}
		});
		$('html').addClass('floating-nav');
	});

	$(function() {
//		var timer;
		$(window).on('scroll', function(event) {
//			if ($.type(timer) === 'number') {
//				clearTimeout(timer);
//				timer = null;
//			}
			var $this = $(this);
//			timer = setTimeout(function() {
				var sTop = $this.scrollTop();
				$('html', document)[(sTop > 0 ? 'addClass' : 'removeClass')]('has-shadow');
//			}, 0);
		});
	});

	function loadPrismTheme(theme) {
		var dfd = $.Deferred();
		var prismTheme = theme;
		$.when(loadStylesheet({
			'selector': '.prism-themes',
			'className': 'prism-themes',
			'src': '/api/jjobs/resources/css/prism-' + theme + '.css'
		}),
		loadScript({
			'selector': '.prism-themes',
			'className': 'prism-themes',
			'src': '/api/jjobs/resources/prism-' + theme + '.js'
		}))
		.then(function() {
			var $enhancedCode = $('.enhanced-code');
			$enhancedCode
			.find('> pre[class*="language-"]')
			.addBack()
			.each(function() {
				var $this = $(this);
				var c = '';
				$.each($.trim($this.attr('class')).split(/\s+/), function(index, value) {
					if (/^prism-theme-/.test(value) && !/prism-theme-picker/.test(value)) {
						if (c.length > 0) {
							c += ' ';
						}
						c += value;
					}
				});
				$this.removeClass(c);
			})
			.addClass('prism-theme-' + prismTheme);

			dfd.resolve();
		});
		return dfd.promise();
	}

	function loadTheme(theme) {
		var dfd = $.Deferred();
		$.when(loadStylesheet({
			'selector': '.jquery-themes',
			'className': 'jquery-themes',
			'src': '//code.jquery.com/ui/1.12.1/themes/' + theme + '/jquery-ui.min.css'
		}))
		.then(function() {
			dfd.resolve();
		});
		return dfd.promise();
	}

	function initThemePicker(themes, defaultTheme) {
		if (arguments.length < 2) {
			defaultTheme = themes[0];
		}
		var lastPageTheme = window.localStorage.getItem('page-theme');
		if (lastPageTheme) {
			defaultTheme = lastPageTheme;
		}
		defaultTheme = defaultTheme.toLowerCase().replace(/\s+/g, '-');
		loadTheme(defaultTheme).always(function() {
			var $select = $();
			$('.page-theme-picker').each(function() {
				var $picker = $(this);
				if ($picker.is('.hasThemePicker')) {
					return true;
				}
				$picker.addClass('hasThemePicker');
				$select = $('<select/>')
				.addClass('theme-picker')
				.appendTo($picker);
				$.each(themes, function(index, value) {
					var key = value.toLowerCase().replace(/\s+/g, '-');
					var $option = $('<option/>')
					.text(value)
					.val(key);
					if (key === defaultTheme) {
						$option.attr('selected', 'selected').prop('selected', true);
					}
					$option.appendTo($select);
				});
				if ($select.selectmenu) {
					$select.selectmenu({
//						'open': function(event, ui) {
//							if (event.originalEvent) {
//								$('.page-theme-picker select.theme-picker', document).not(this)
//								.selectmenu('open');
//							}
//						},
//						'close': function(event, ui) {
//							if (event.originalEvent) {
//								$('.page-theme-picker select.theme-picker', document).not(this)
//								.selectmenu('close');
//							}
//						},
						'open': function(event, ui) {
							var $this = $(this);
							var $menu = $this.selectmenu('menuWidget');
							var $selectMenu = $menu.closest('.ui-selectmenu-menu');

							$.each($.trim($selectMenu.attr('class')).split(/\s+/), function(index, value) {
								if (/^ui-corner-/.test(value)) {
									$selectMenu.removeClass(value);
								}
							});
							$.each($.trim($menu.attr('class')).split(/\s+/), function(index, value) {
								if (/^ui-corner-/.test(value)) {
									$selectMenu.addClass(value);
								}
							});
						},
						'appendTo': $picker,
						'position': {
							'my': 'right top',
							'at': 'right bottom',
							'collision': 'flipfit'
						},
						'change': function(event, ui) {
							var $this = $(this);
							var v = $this.val();
							window.localStorage.setItem('page-theme', v);
							$('.page-theme-picker select.theme-picker', document).not(this)
							.children('option[value="' + v + '"]:first')
							.prop('selected', true)
							.end()
							.selectmenu('refresh');
							loadTheme(v);
						}
					});
					$('<label/>')
					.attr({
						'for': $select.attr('id')
					})
					.addClass('theme-picker')
					.text('Theme:')
					.insertBefore($select);
				}
			});
		});
	}

	function loadScript(path) {
		var dfd = $.Deferred();
		var $script = $();
		var src;
		var className;
		if ($.isPlainObject(path)) {
			src = $.trim(path.src);
			if (path.selector) {
				$script = $('script').filter(path.selector);
			}
			if (path.className) {
				className = path.className;
			}
		} else {
			src = $.trim(path);
		}

		var $oldScripts = $script;
		$script = $('<script/>')
		.attr({
			'type': 'text/javascript'
		})
		.appendTo('head');
		if (className) {
			$script.addClass(className);
		}
		$script.one('load', function(event) {
			$oldScripts.remove();
			dfd.resolve();
		})
		.one('error', function(event) {
			$oldScripts.remove();
			dfd.reject();
		});
		$script
		.attr({
			'src': src
		});
		return dfd.promise();
	}

	function loadStylesheet(path) {
		var dfd = $.Deferred();
		var $link = $();
		var src;
		var className;
		if ($.isPlainObject(path)) {
			src = $.trim(path.src);
			if (path.selector) {
				$link = $('link').filter(path.selector);
			}
			if (path.className) {
				className = path.className;
			}
		} else {
			src = $.trim(path);
		}

		var $oldLinks = $link;
		$link = $('<link/>')
		.attr({
			'type': 'text/css',
			'rel': 'stylesheet'
		})
		.appendTo('head');
		if (className) {
			$link.addClass(className);
		}

		$link.one('load', function(event) {
			$oldLinks.remove();
			dfd.resolve();
		})
		.one('error', function(event) {
			$oldLinks.remove();
			dfd.reject();
		});
		$link
		.attr({
			'href': src
		});

		return dfd.promise();
	}
})(jQuery, document, window);
