import instaloader

def download(profile):
    profile = profile.replace(" ", "")
    user = instaloader.Instaloader()
    user.save_metadata = False
    user.download_video_thumbnails = False
    user.post_metadata_txt_pattern = ""
    user.dirname_pattern = f"/storage/emulated/0/Download/InstaDownloader{profile}"
    user.download_profile(profile)


def post_count(username):
    username = username.replace(" ", "")
    L = instaloader.Instaloader()
    profile = instaloader.Profile.from_username(L.context, username)

    posts = profile.get_posts()

    return posts.count

# for reference: https://github.com/instaloader/instaloader/issues/1851
def download_post_from_link(shortcode,file_Name):
    L = instaloader.Instaloader()
    L.save_metadata = False
    L.download_video_thumbnails = False
    L.post_metadata_txt_pattern = ""
    L.filename_pattern = file_Name
    L.dirname_pattern = f"/storage/emulated/0/Download/InstaDownloader"
    post = instaloader.Post.from_shortcode(L.context, shortcode)
    L.download_post(post, target = "")
    caption = post.caption
    username = post.profile
    post_url = post.url
             
    print(f"{username} : {caption}")
    return caption, username, post_url