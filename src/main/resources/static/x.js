// XSPA 사용을 위해 반드시 호출할 것
function initX() {
    let I18N;
    $.getJSON("json/i18n.json", function (data) {
        I18N = data;
    }).fail(function () {
        Alert("i18n.json 로딩 실패");
    });

    let LANG = localStorage.getItem("lang");
    if (!LANG) {
        LANG = "ko"; // 기본값
        localStorage.setItem("LANG", LANG);
    }
    function MSG(key, data) {
        console.log(key, data);
        let msg = I18N[LANG] && I18N[LANG][key] ? I18N[LANG][key] : key;
        if (Array.isArray(data)) {
            data.forEach(function (val, idx) {
                const regex = new RegExp("\\$\\{" + idx + "\\}", "g");
                msg = msg.replace(regex, val);
            });
        }
        return msg;
    };

    // === 공용 오버레이 ===
    let overlay = $("<div>", { id: "xspaOverlay" });
    $("body").append(overlay);

    let queue = [];
    let active = false;

    function show(contentHtml) {
        if (active) { queue.push(() => show(contentHtml)); return; }
        overlay.html(contentHtml).css("display", "flex");
        active = true;
    }

    function hide() {
        overlay.hide().empty();
        active = false;
        if (queue.length > 0) { let next = queue.shift(); next(); }
    }

    // Alert
    function Alert(msg) {
        show(`
            <div class="xspa-modal">
                <div>${msg}</div>
                <button onclick="X.HideModal()">OK</button>
            </div>
        `);
    }

    // Confirm
    function Confirm(msg, onYes, onNo) {
        function click(flag) {
            X.HideModal();
            if (flag) {
                if (typeof onYes === "function") onYes();
            } else {
                if (typeof onNo === "function") onNo();
            }
        }
        show(`
            <div class="xspa-modal">
                <div>${msg}</div>
                <button onclick="click(true)">OK</button>
                <button onclick="click(false)">Cancel</button>
            </div>
        `);
    }

    // Popup
    function Popup(html) {
        show(`
            <div class="xspa-modal">
                <div class="close" onclick="X.HideModal()">×</div>
                ${html}
            </div>
        `);
    }

    // 배경 클릭 / ESC 닫기
    overlay.on("click", function (e) {
        if (e.target.id === "xspaOverlay") hide();
    });
    $(document).on("keydown", function (e) {
        if (e.key === "Escape") hide();
    });

    // === Loading 전용 레이어 ===
    let loadingLayer = $("<div>", { id: "xspaLoading" });
    $("body").append(loadingLayer);

    function Loading() {
        loadingLayer.html(`<div class="spinner"></div>`)
            .css("z-index", 10000)
            .show();
    }

    function HideLoading() {
        loadingLayer.hide().empty()
            .css("z-index", -1);
    }

    function Call(o) {
        Loading();

        if (!o.timeout) o.timeout = 3000;
        console.log("요청", o);

        var headers = {
            "Parser": o.parser
        };
        var token = sessionStorage.getItem("XToken");
        if (token) {
            headers["XToken"] = token;
        }
        $.ajax({
            url: o.url,
            type: "POST",
            data: JSON.stringify(o.data),
            contentType: "application/json; charset=UTF-8",
            dataType: "json",
            timeout: o.timeout,
            headers: headers,
            success: function (r) {
                console.log("응답", r);
                if (r.code == "OK") {
                    if (o.success) o.success(r);
                } else {
                    if (o.error) o.error(r);
                    else Alert(MSG(r.code, r.data));
                }
            },
            error: function (xhr, status, error) {
                if (status === "timeout") {
                    Alert("요청타임아웃");
                } else {
                    Alert(xhr.status + " | " + status + " | " + error);
                }
            },
            complete: function () {
                HideLoading();
            }
        });
    }

    function FormToJson(event) {
        event.preventDefault();

        var form = $(event.target);          // 이벤트가 발생한 form
        var procName = form.attr("id");      // form의 id → SP 이름
        var formArray = form.serializeArray();

        var params = {};
        $.each(formArray, function (_, field) {
            params[field.name] = field.value;
        });

        // 최종 JSON 문자열 반환
        return ([
            { [procName]: params }
        ]);
    }


    function router() {
        console.log("라우터호출");
        var hash = location.hash.replace('#', '') || "home";
        var page = "pages/" + hash + ".html";
        $('#content').load(page);
    }
    $(window).on('hashchange', router);
    router();

    // X 네임스페이스로 노출
    window.X = {
        LANG: LANG,
        MSG: MSG,
        Alert: Alert,
        Confirm: Confirm,
        Popup: Popup,
        HideModal: hide,
        Loading: Loading,
        HideLoading: HideLoading,
        FormToJson: FormToJson,
        Call: Call
    };
}
