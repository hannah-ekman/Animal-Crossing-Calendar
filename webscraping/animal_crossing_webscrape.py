from bs4 import BeautifulSoup as bs
#urllib3 or urllib.reqest
import urllib
import requests
from lxml import html
import os

url = 'https://animalcrossing.fandom.com/wiki/Fish_(New_Horizons)'
#getting the webpage
r = requests.get(url)

#creating the directory to store downloaded images
curr_dir = os.getcwd()
folder = 'acPic'
dir_path = os.path.join(curr_dir, folder)

if not os.path.exists(dir_path):
    os.mkdir(dir_path)

#function for downloading the images and setting the name
def download_img(url):
    image_name = str(url.split('/')[-3])
    urllib.request.urlretrieve(url, os.path.join(dir_path, image_name))


#beautifulsoup object with raw HTML content and parser passed in
#lxml is the default parser
soup = bs(r.content, features="lxml")
for image in soup.findAll('a', {"class": "image image-thumbnail"}):
    print(image['href'])
    download_img(image['href'])
    